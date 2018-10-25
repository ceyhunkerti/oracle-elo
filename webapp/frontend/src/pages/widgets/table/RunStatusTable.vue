<template lang="pug">
v-data-table(:items='items' hide-actions)
  template(slot='items' slot-scope='props')
    td
      v-chip(slot="activator" outline small label :color="itemColor(props.item.name)" ) {{ props.item.name }}
    td.text-xs-right {{ props.item.value }}
</template>

<script>

import _ from 'lodash'

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
        const value = this.data[k]
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
