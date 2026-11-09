<template>
  <div class="bi">
    <div class="bi-toolbar">
      <div class="bi-title">运营分析</div>
      <div class="bi-filters">
        <span class="filter-label">统计周期</span>
        <el-select v-model="days" size="small" style="width: 110px" @change="load">
          <el-option :value="7" label="近 7 天" />
          <el-option :value="14" label="近 14 天" />
          <el-option :value="30" label="近 30 天" />
        </el-select>
        <el-button size="small" :loading="loading" @click="load">刷新数据</el-button>
      </div>
    </div>

    <div class="bi-banner">交易分析</div>

    <div v-if="loading && !ready" class="bi-loading">加载中…</div>

    <template v-else>
      <div class="bi-top">
        <div class="metric-col">
          <div v-for="m in metrics" :key="m.label" class="metric-card">
            <div class="metric-main">
              <div class="metric-value">{{ m.value }}</div>
              <div class="metric-label">{{ m.label }}</div>
            </div>
            <div class="metric-tags">
              <span class="tag" :class="m.yoy >= 0 ? 'up' : 'down'">
                同比 {{ fmtPct(m.yoy) }}
              </span>
              <span class="tag" :class="m.mom >= 0 ? 'up' : 'down'">
                环比 {{ fmtPct(m.mom) }}
              </span>
            </div>
          </div>
        </div>

        <div class="gauge-col">
          <div class="panel">
            <div class="panel-title">成交完成率</div>
            <div ref="gaugeDoneRef" class="chart gauge" />
          </div>
          <div class="panel">
            <div class="panel-title">商品售出率</div>
            <div ref="gaugeSoldRef" class="chart gauge" />
          </div>
        </div>

        <div class="panel trend-panel">
          <div class="panel-title">成交额日趋势【联动下】</div>
          <div ref="amountTrendRef" class="chart tall" />
        </div>
      </div>

      <div class="bi-bottom">
        <div class="panel">
          <div class="panel-title">订单量日趋势</div>
          <div ref="orderTrendRef" class="chart mid" />
        </div>
        <div class="panel">
          <div class="panel-title">完成订单日趋势</div>
          <div ref="doneTrendRef" class="chart mid" />
        </div>
        <div class="panel">
          <div class="panel-title">分类商品分布</div>
          <div ref="categoryRef" class="chart mid" />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { adminStatistics, adminTradeStatistics, adminCategoryStatistics } from '../../api/admin.js'

const TEAL = '#14b8a6'
const TEAL_DARK = '#0d9488'
const BLUE = '#3b82f6'
const BLUE_SOFT = '#93c5fd'

const days = ref(30)
const loading = ref(false)
const ready = ref(false)
const stats = ref(null)
const trade = ref(null)
const categories = ref([])

const gaugeDoneRef = ref(null)
const gaugeSoldRef = ref(null)
const amountTrendRef = ref(null)
const orderTrendRef = ref(null)
const doneTrendRef = ref(null)
const categoryRef = ref(null)

const charts = []

function pctChange(cur, prev) {
  const c = Number(cur) || 0
  const p = Number(prev) || 0
  if (p === 0) return c > 0 ? 100 : 0
  return Number((((c - p) / p) * 100).toFixed(1))
}

function fmtPct(v) {
  const n = Number(v) || 0
  return `${n >= 0 ? '+' : ''}${n}%`
}

function money(v) {
  const n = Number(v) || 0
  if (n >= 10000) return `${(n / 10000).toFixed(2)} 万元`
  return `¥${n.toFixed(2)}`
}

const metrics = computed(() => {
  const o = stats.value?.orders || {}
  const u = stats.value?.users || {}
  return [
    {
      label: '交易总额',
      value: money(o.totalAmount),
      yoy: pctChange(o.amountWeek, o.amountPrevWeek),
      mom: pctChange(o.countWeek, o.countPrevWeek),
    },
    {
      label: '订单总数',
      value: String(o.total ?? 0),
      yoy: pctChange(o.countWeek, o.countPrevWeek),
      mom: pctChange(o.completed, Math.max(Number(o.total || 0) - Number(o.completed || 0), 1)),
    },
    {
      label: '用户总数',
      value: String(u.total ?? 0),
      yoy: pctChange(u.newWeek, u.newPrevWeek),
      mom: pctChange(u.newToday, Math.max(Math.floor((Number(u.newWeek) || 0) / 7) || 1, 1)),
    },
  ]
})

const completionRate = computed(() => {
  const o = stats.value?.orders || {}
  const total = Number(o.total) || 0
  if (!total) return 0
  return Number((((Number(o.completed) || 0) / total) * 100).toFixed(1))
})

const soldRate = computed(() => {
  const p = stats.value?.products || {}
  const total = Number(p.total) || 0
  if (!total) return 0
  return Number((((Number(p.sold) || 0) / total) * 100).toFixed(1))
})

function disposeCharts() {
  while (charts.length) {
    charts.pop()?.dispose()
  }
}

function makeChart(el) {
  if (!el) return null
  const c = echarts.init(el)
  charts.push(c)
  return c
}

function gaugeOption(value, name) {
  return {
    series: [
      {
        type: 'gauge',
        startAngle: 210,
        endAngle: -30,
        min: 0,
        max: 100,
        radius: '88%',
        center: ['50%', '58%'],
        axisLine: {
          lineStyle: {
            width: 14,
            color: [
              [value / 100, TEAL],
              [1, '#e5e7eb'],
            ],
          },
        },
        pointer: { show: false },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { show: false },
        detail: {
          valueAnimation: true,
          fontSize: 26,
          fontWeight: 700,
          color: '#0f172a',
          formatter: '{value}%',
          offsetCenter: [0, '8%'],
        },
        title: {
          show: true,
          offsetCenter: [0, '42%'],
          fontSize: 12,
          color: '#64748b',
        },
        data: [{ value, name }],
      },
    ],
  }
}

function comboOption(labels, barData, lineData, barName, lineName) {
  return {
    color: [BLUE, TEAL],
    tooltip: { trigger: 'axis' },
    legend: {
      bottom: 0,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: '#64748b', fontSize: 11 },
      data: [barName, lineName],
    },
    grid: { left: 42, right: 18, top: 28, bottom: 36 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#94a3b8', fontSize: 10 },
      axisTick: { show: false },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f1f5f9' } },
      axisLabel: { color: '#94a3b8', fontSize: 10 },
    },
    series: [
      {
        name: barName,
        type: 'bar',
        barWidth: '45%',
        itemStyle: { color: BLUE, borderRadius: [3, 3, 0, 0] },
        data: barData,
      },
      {
        name: lineName,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        lineStyle: { width: 2, color: TEAL },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(20,184,166,.35)' },
            { offset: 1, color: 'rgba(20,184,166,.02)' },
          ]),
        },
        data: lineData,
      },
    ],
  }
}

function categoryOption(list) {
  const names = list.map((i) => i.name)
  const counts = list.map((i) => i.count)
  return {
    color: [TEAL_DARK, BLUE_SOFT],
    tooltip: { trigger: 'axis' },
    legend: {
      bottom: 0,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: '#64748b', fontSize: 11 },
      data: ['商品数'],
    },
    grid: { left: 42, right: 12, top: 28, bottom: 36 },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { color: '#94a3b8', fontSize: 10, rotate: names.length > 6 ? 28 : 0 },
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#e2e8f0' } },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f1f5f9' } },
      axisLabel: { color: '#94a3b8', fontSize: 10 },
    },
    series: [
      {
        name: '商品数',
        type: 'bar',
        barWidth: '48%',
        itemStyle: {
          borderRadius: [3, 3, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: TEAL },
            { offset: 1, color: TEAL_DARK },
          ]),
        },
        data: counts,
      },
    ],
  }
}

function renderCharts() {
  disposeCharts()
  const labels = trade.value?.labels || []
  const amounts = (trade.value?.amounts || []).map((v) => Number(v) || 0)
  const orderCounts = trade.value?.orderCounts || []
  const completedCounts = trade.value?.completedCounts || []

  const g1 = makeChart(gaugeDoneRef.value)
  g1?.setOption(gaugeOption(completionRate.value, '成交完成率'))
  const g2 = makeChart(gaugeSoldRef.value)
  g2?.setOption(gaugeOption(soldRate.value, '商品售出率'))

  const a = makeChart(amountTrendRef.value)
  a?.setOption(comboOption(labels, amounts, amounts, '本期成交额', '趋势'))

  const o = makeChart(orderTrendRef.value)
  o?.setOption(comboOption(labels, orderCounts, completedCounts, '下单量', '完成量'))

  const d = makeChart(doneTrendRef.value)
  d?.setOption(comboOption(labels, completedCounts, amounts, '完成单', '成交额'))

  const c = makeChart(categoryRef.value)
  c?.setOption(categoryOption(categories.value.length ? categories.value : [{ name: '暂无', count: 0 }]))
}

async function load() {
  loading.value = true
  try {
    const [sRes, tRes, cRes] = await Promise.all([
      adminStatistics(),
      adminTradeStatistics(days.value),
      adminCategoryStatistics(),
    ])
    if (sRes.code === 200) stats.value = sRes.data
    if (tRes.code === 200) trade.value = tRes.data
    if (cRes.code === 200) categories.value = cRes.data?.categories || []
    ready.value = true
    await nextTick()
    renderCharts()
  } catch {
    /* ignore */
  } finally {
    loading.value = false
  }
}

function onResize() {
  charts.forEach((c) => c.resize())
}

onMounted(() => {
  load()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  disposeCharts()
})
</script>

<style scoped>
.bi {
  --teal: #14b8a6;
  --teal-dark: #0d9488;
  --panel: #fff;
  --bg: #f1f5f9;
  min-height: 100%;
  padding: 4px 2px 8px;
  background: var(--bg);
}

.bi-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.bi-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.bi-filters {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 6px 10px;
}

.filter-label {
  font-size: 12px;
  color: #64748b;
}

.bi-banner {
  height: 44px;
  border-radius: 6px;
  background: linear-gradient(90deg, #0d9488, #14b8a6 55%, #2dd4bf);
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.12em;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  box-shadow: 0 6px 16px rgba(13, 148, 136, 0.22);
}

.bi-loading {
  padding: 48px;
  text-align: center;
  color: #64748b;
}

.bi-top {
  display: grid;
  grid-template-columns: 200px 320px 1fr;
  gap: 12px;
  margin-bottom: 12px;
}

.metric-col {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.metric-card {
  background: var(--panel);
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  padding: 14px 12px 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 108px;
}

.metric-value {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.2;
}

.metric-label {
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}

.metric-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
  border: 1px solid transparent;
}

.tag.up {
  color: #059669;
  background: #ecfdf5;
  border-color: #a7f3d0;
}

.tag.down {
  color: #dc2626;
  background: #fef2f2;
  border-color: #fecaca;
}

.gauge-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.panel {
  background: var(--panel);
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px 12px 8px;
  min-width: 0;
}

.panel-title {
  font-size: 13px;
  font-weight: 700;
  color: #334155;
  margin-bottom: 2px;
}

.chart {
  width: 100%;
}

.chart.gauge {
  height: 168px;
}

.chart.tall {
  height: 236px;
}

.chart.mid {
  height: 260px;
}

.trend-panel {
  min-height: 280px;
}

.bi-bottom {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

@media (max-width: 1200px) {
  .bi-top {
    grid-template-columns: 1fr;
  }
  .gauge-col {
    grid-template-columns: 1fr 1fr;
  }
  .bi-bottom {
    grid-template-columns: 1fr;
  }
}
</style>
