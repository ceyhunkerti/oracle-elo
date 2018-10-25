<template lang="pug">
#pageDashboard
  v-container(grid-list-xl fluid)
    v-layout(row wrap)
      // mini statistic start
      v-flex(lg3 sm6 xs12)
        mini-statistic(icon='fa fa-database' :title="'' + dbLinks.length" sub-title='Sources' color='success')
      v-flex(lg3 sm6 xs12)
        mini-statistic(icon='fa fa-th-large' :title="'' + targetTables.length" sub-title='Tables' color='error')
      v-flex(lg3 sm6 xs12)
        mini-statistic(icon='fa fa-align-justify' :title="'' + columns.length" sub-title='Columns' color='light-blue')
      v-flex(lg3 sm6 xs12)
        mini-statistic(icon='fa fa-save' :title='size' sub-title='Size' color='warning')
    v-layout(row wrap)
      v-flex(lg12 sm12 xs12)
        span {{status.success}} / {{status.total}}
        v-progress-linear(:color="progressColor" height="20" :value="successProgress")
    v-layout(row wrap)
      v-flex(lg4 sm4 xs12)
        run-status-pie-chart(:data="status")
      v-flex(lg4 sm4 xs12)
        run-status-gauge-chart(:data="status")
      v-flex(lg4 sm4 xs12)
        run-status-table.run-status-table(:data="status" run-status-table)

</template>

<script>

import _ from 'lodash'
import { mapGetters, mapActions } from 'vuex'
import MiniStatistic from '@/components/widgets/statistic/MiniStatistic';
import colors from 'vuetify/es5/util/colors'
import Cookies from 'js-cookie'
import RunStatusPieChart from '@/pages/widgets/chart/RunStatusPieChart'
import RunStatusGaugeChart from '@/pages/widgets/chart/RunStatusGaugeChart'
import RunStatusTable from '@/pages/widgets/table/RunStatusTable'

export default {
  components: {
    MiniStatistic,
    RunStatusPieChart,
    RunStatusGaugeChart,
    RunStatusTable
  },
  data() {
    return {
      size: '...'
    }
  },
  computed: {
    ...mapGetters('elo', [
      'maps',
      'status'
    ]),
    successProgress() {
      return 100 * (this.status.success / this.status.total)
    },
    progressColor() {
      if (this.status.error > 0) {
        return 'error'
      } else if (this.status.success === 0) {
        return 'warning'
      } else if (this.status.success === this.status.total) {
        return 'success'
      } else {
        return 'primary'
      }
    },
    dbLinks() {
      return _.chain(this.maps).map(m => m.dbLink).uniq().value()
    },
    targetTables() {
      return _.map(this.maps, m => `${m.targetSchema}.${m.targetTable}`)

    },
    columns() {
      return _.flatMap(this.maps, m => m.columns)
    }
  },
  methods: {
    ...mapActions('elo', [
      'findEloSize'
    ]),
    init() {
      this.findEloSize().then(response => {
        this.size = `${response.data} GB`
      })
    }
  },
  mounted() {
    this.init()
    const val = Cookies.get('theme-primary')
    const base = colors[val] ? colors[val].base : colors["blue"]
    this.$vuetify.theme.primary = base
  }

};
</script>

<style >
.run-status-table[run-status-table] .theme--light.v-table{
  background-color: transparent;
}
</style>

