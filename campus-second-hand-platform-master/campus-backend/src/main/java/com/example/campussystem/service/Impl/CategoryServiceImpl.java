package com.example.campussystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.campussystem.entity.Category;
import com.example.campussystem.mapper.CategoryMapper;
import com.example.campussystem.mapper.ProductMapper;
import com.example.campussystem.service.CategoryService;
import com.example.campussystem.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CacheService cacheService;

    private static final String CACHE_KEY_ALL_CATEGORIES = "all_categories";
    private static final String CACHE_KEY_CATEGORY_TREE = "category_tree";
    private static final String CACHE_KEY_CATEGORY_BY_PARENT = "category_by_parent:";
    private static final String CACHE_KEY_CATEGORY_DETAIL = "category_detail:";

    public Category createCategory(String name, Long parentId, Integer sortOrder) {
        var existing = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, name)
                .eq(Category::getParentId, parentId));
        if (existing != null) {
            throw new RuntimeException("同级分类中已存在相同名称的分类");
        }

        if (sortOrder == null) {
            var maxCategory = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                    .eq(Category::getParentId, parentId)
                    .orderByDesc(Category::getSortOrder)
                    .last("LIMIT 1"));
            int maxSort = maxCategory.isEmpty() ? 0 : maxCategory.get(0).getSortOrder();
            sortOrder = maxSort + 1;
        }

        Category category = new Category();
        category.setName(name);
        category.setParentId(parentId);
        category.setSortOrder(sortOrder);
        category.setStatus(1);

        categoryMapper.insert(category);
        clearCategoryCaches();
        return category;
    }

    public Category updateCategory(Long id, String name, Long parentId, Integer sortOrder, Integer status) {
        Category category = getCategoryById(id);

        if (!category.getName().equals(name) || !category.getParentId().equals(parentId)) {
            var existing = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                    .eq(Category::getName, name)
                    .eq(Category::getParentId, parentId));
            if (existing != null && !existing.getId().equals(id)) {
                throw new RuntimeException("同级分类中已存在相同名称的分类");
            }
        }

        if (parentId != null && !parentId.equals(0L) && isDescendant(parentId, id)) {
            throw new RuntimeException("不能将分类移动到自己的子分类下");
        }

        category.setName(name);
        if (parentId != null) category.setParentId(parentId);
        if (sortOrder != null) category.setSortOrder(sortOrder);
        if (status != null) category.setStatus(status);

        categoryMapper.updateById(category);
        clearCategoryCaches();
        return category;
    }

    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);

        if (categoryMapper.exists(new LambdaQueryWrapper<Category>().eq(Category::getParentId, id))) {
            throw new RuntimeException("该分类下还有子分类，无法删除");
        }

        if (productMapper.selectCount(new LambdaQueryWrapper<com.example.campussystem.entity.Product>()
                .eq(com.example.campussystem.entity.Product::getCategoryId, id)) > 0) {
            throw new RuntimeException("该分类下还有商品，无法删除");
        }

        categoryMapper.deleteById(id);
        clearCategoryCaches();
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        String cacheKey = CACHE_KEY_CATEGORY_DETAIL + id;

        Category cached = cacheService.get(cacheKey, Category.class);
        if (cached != null) return cached;

        Category category = categoryMapper.selectById(id);
        if (category == null) throw new RuntimeException("分类不存在");

        cacheService.set(cacheKey, category, 60, java.util.concurrent.TimeUnit.MINUTES);
        return category;
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        @SuppressWarnings("unchecked")
        List<Category> cached = cacheService.get(CACHE_KEY_ALL_CATEGORIES, List.class);
        if (cached != null) return cached;

        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder));

        cacheService.set(CACHE_KEY_ALL_CATEGORIES, categories, 60, java.util.concurrent.TimeUnit.MINUTES);
        return categories;
    }

    @Transactional(readOnly = true)
    public List<Category> getAllActiveCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder));
    }

    @Transactional(readOnly = true)
    public List<Category> getTopLevelCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, 0L)
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder));
    }

    @Transactional(readOnly = true)
    public List<Category> getCategoriesByParentId(Long parentId) {
        String cacheKey = CACHE_KEY_CATEGORY_BY_PARENT + parentId;

        @SuppressWarnings("unchecked")
        List<Category> cached = cacheService.get(cacheKey, List.class);
        if (cached != null) return cached;

        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, parentId)
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder));

        cacheService.set(cacheKey, categories, 30, java.util.concurrent.TimeUnit.MINUTES);
        return categories;
    }

    @Transactional(readOnly = true)
    public List<Category> getCategoryTree() {
        List<Category> topCategories = getTopLevelCategories();
        for (Category category : topCategories) {
            loadChildren(category);
        }
        return topCategories;
    }

    private void loadChildren(Category category) {
        List<Category> children = getCategoriesByParentId(category.getId());
        category.setChildren(children);
        for (Category child : children) {
            loadChildren(child);
        }
    }

    private boolean isDescendant(Long ancestorId, Long descendantId) {
        if (ancestorId.equals(descendantId)) return true;
        List<Category> children = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, descendantId)
                .orderByAsc(Category::getSortOrder));
        for (Category child : children) {
            if (isDescendant(ancestorId, child.getId())) return true;
        }
        return false;
    }

    public Category toggleCategoryStatus(Long id) {
        Category category = getCategoryById(id);
        category.setStatus(category.getStatus() == 1 ? 0 : 1);
        categoryMapper.updateById(category);
        clearCategoryCaches();
        return category;
    }

    public void deleteCategoriesBatch(List<Long> ids) {
        for (Long id : ids) {
            deleteCategory(id);
        }
    }

    public void initializeBasicCategories() {
        if (categoryMapper.selectCount(null) > 0) return;

        Category electronics = createCategory("电子产品", 0L, 1);
        Category books = createCategory("图书教材", 0L, 2);
        Category clothing = createCategory("服装配饰", 0L, 3);
        Category sports = createCategory("运动户外", 0L, 4);
        Category daily = createCategory("生活用品", 0L, 5);
        Category other = createCategory("其他", 0L, 6);

        createCategory("手机数码", electronics.getId(), 1);
        createCategory("电脑配件", electronics.getId(), 2);
        createCategory("耳机音响", electronics.getId(), 3);
        createCategory("智能设备", electronics.getId(), 4);

        createCategory("专业教材", books.getId(), 1);
        createCategory("考试资料", books.getId(), 2);
        createCategory("小说文学", books.getId(), 3);
        createCategory("工具书", books.getId(), 4);

        createCategory("男装", clothing.getId(), 1);
        createCategory("女装", clothing.getId(), 2);
        createCategory("鞋靴", clothing.getId(), 3);
        createCategory("包包配饰", clothing.getId(), 4);

        createCategory("运动鞋服", sports.getId(), 1);
        createCategory("健身器材", sports.getId(), 2);
        createCategory("户外用品", sports.getId(), 3);
        createCategory("球类运动", sports.getId(), 4);

        createCategory("宿舍用品", daily.getId(), 1);
        createCategory("学习用品", daily.getId(), 2);
        createCategory("护肤美妆", daily.getId(), 3);
        createCategory("食品零食", daily.getId(), 4);
    }

    private void clearCategoryCaches() {
        cacheService.delete(CACHE_KEY_ALL_CATEGORIES);
        cacheService.delete(CACHE_KEY_CATEGORY_TREE);
        cacheService.deleteByPattern(CACHE_KEY_CATEGORY_BY_PARENT + "*");
        cacheService.deleteByPattern(CACHE_KEY_CATEGORY_DETAIL + "*");
    }
}
