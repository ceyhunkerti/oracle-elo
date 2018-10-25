<template lang="pug">
  div(:style="{height:height,width:width}")
</template>

<script>
import _ from 'lodash'
import echarts from 'echarts'
require('echarts/theme/macarons') // echarts theme
import { debounce } from '@/util'

export default {
  props: {
    className: { type: String, default: 'chart' },
    width: { type: String, default: '100%' },
    height: { type: String, default: '400px' },
    data: { type: Object, required: true}
  },
  data() {
    return {
      chart: null
    }
  },
  mounted() {
    this.initChart()
    this.__resizeHanlder = debounce(() => {
      if (this.chart) {
        this.chart.resize()
      }
    }, 100)
    window.addEventListener('resize', this.__resizeHanlder)
  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    window.removeEventListener('resize', this.__resizeHanlder)
    this.chart.dispose()
    this.chart = null
  },
  watch: {
    'data'() {
      this.initChart()
    }
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$el, 'macarons')
      const names = ['Ready', 'Success', 'Error', 'Running']
      const data = [
        { name: "Ready", value: this.data.ready,  itemStyle: {color: '#FFC107'} },
        { name: "Success", value: this.data.success, itemStyle: {color: '#4CAF50'} },
        { name: "Error", value: this.data.error, itemStyle: {color: '#FF5252'} },
        { name: "Running", value: this.data.running, itemStyle: {color: '#03A9F4'} }
      ]

      this.chart.setOption({
        title : {
            text: 'Status Totals',
            subtext: 'count of each status in extraction',
            x:'center'
        },
        tooltip : {
          trigger: 'item',
          formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        legend: {
          x : 'center',
          y : 'bottom',
          data: names
        },
        toolbox: {
          show : true,
          feature : {
            mark : {show: true},
            dataView : {
              show: true, readOnly: true, title: 'Data view',
              lang: ['Data view', 'Close', 'Reload']
            },
            magicType : {
                show: true,
                type: ['pie', 'funnel']
            },
            restore : {show: true, title: 'Restore'},
            saveAsImage : {show: true, title: 'Save as image'}
          }
        },
        calculable : true,
        series : [
          {
            name:'Status Totals',
            type:'pie',
            radius : [20, 110],
            roseType : 'radius',
            itemStyle : {
                normal : {
                    label : {
                        show : false
                    },
                    labelLine : {
                        show : false
                    }
                },
                emphasis : {
                    label : {
                        show : true
                    },
                    labelLine : {
                        show : true
                    }
                }
            },
            data: data
          }
        ]
      })
    }
  }
}
</script>
