<template lang="pug">
v-data-table(:items='items' hide-actions)
  template(slot='items' slot-scope='props')
    td
      v-chip(slot="activator" outline small label :color="itemColor(props.item.name)" ) {{ props.item.name }}
    td.text-xs-right {{ props.item.value }}
</template>

<script>

import _ from 'lodash'
import moment from 'moment'

export default {
  props: {
    data: { type: Object, required: true}
  },
  data() {
    return {
    }
  },
  computed: {
    items() {
      return _.chain(this.data).keys().map(k => {
        const name = _.capitalize(k)
        let value = ((k === 'startTime' || k === 'endTime') ? moment(this.data[k]).format("HH:mm:ss") : this.data[k])
        if (k === 'endTime' && (this.data.running > 0 || this.data.ready > 0)) {
          value = moment().format("HH:mm:ss")
        }
        return { name, value }
      }).value()
    }
  },
  methods: {
    itemColor(name) {
      switch (name.toLowerCase()) {
        case 'ready': return '#FFC107'
        case 'running': return '#03A9F4'
        case 'error': return '#FF5252'
        case 'success': return '#4CAF50'
        case 'total': return 'black'
      }
    }
  }
}
</script>
