<template lang="pug">
#page-toolbars
  v-container(grid-list-xl='' fluid='')
    v-layout(row='' wrap='')
      v-flex(xs12='')
        v-widget(title='Basic Usage')
          div(slot='widget-content')
            div
              v-subheader Default
              v-toolbar(dark='' color='primary')
                v-toolbar-side-icon
                v-toolbar-title.white--text Title
                v-spacer
                v-btn(icon='')
                  v-icon search
                v-btn(icon='')
                  v-icon apps
                v-btn(icon='')
                  v-icon refresh
                v-btn(icon='')
                  v-icon more_vert
            div
              v-subheader Dense
              v-toolbar(dark='' color='purple' dense='')
                v-toolbar-side-icon
                v-toolbar-title.white--text Title
                v-spacer
                v-btn(icon='')
                  v-icon search
                v-btn(icon='')
                  v-icon apps
                v-btn(icon='')
                  v-icon refresh
                v-btn(icon='')
                  v-icon more_vert
            div
              v-subheader Link items
              v-toolbar
                v-spacer
                v-toolbar-items
                  v-btn(flat='') Link One
                  v-btn(flat='') Link Two
                  v-btn(flat='') Link Three
            div
              v-subheader Extended
              v-toolbar(dark='' color='primary' extended='')
                v-toolbar-side-icon
                v-toolbar-title.white--text(slot='extension') Title
                v-spacer
                v-btn(icon='')
                  v-icon search
                v-btn(icon='')
                  v-icon apps
                v-btn(icon='')
                  v-icon refresh
                v-btn(icon='')
                  v-icon more_vert
      v-flex(xs12='')
        v-card(flat='')
          v-toolbar(dark='' color='primary' flat='' extended='')
            v-toolbar-side-icon
            v-toolbar-title Complex usage in page layout
          v-layout(row='' pb-2='')
            v-flex(xs8='' offset-xs2='')
              v-card.card--flex-toolbar
                v-toolbar(card='' prominent='')
                  v-toolbar-title.body-2.grey--text Title
                  v-spacer
                  v-btn(icon='')
                    v-icon search
                  v-btn(icon='')
                    v-icon apps
                  v-btn(icon='')
                    v-icon more_vert
                v-divider
                v-card-text

</template>

<script>
import VWidget from '@/components/VWidget';
export default {
  components: {
    VWidget
  },
  data () {
    return {
    };
  },
  computed: {
  },
  methods: {
  }
};
</script>

<style lang="css" scoped>
  .card--flex-toolbar {
    margin-top: -64px;
  }
</style>