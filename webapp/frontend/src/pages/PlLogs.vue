<template lang="pug">
#extractions
  v-dialog(v-model='dialog.visible' width='500')
    v-card
      v-card-text
        codemirror(v-model="dialog.content" :options="cmOptions")
      v-divider
      v-card-actions
        v-spacer
        v-btn(color='primary' flat='' @click='(e) => onCopyDialog(e)')
          | Copy
        v-btn(color='primary' flat='' @click='dialog.visible = false')
          | Close
  v-container(grid-list-xl fluid)
    v-layout(v-show="false" align-center justify-center row fill-height)
      v-btn(large color="success") Create Map
        v-icon(right dark) add
    v-layout(v-show="true" row wrap)
      v-flex(sm12)
        h3 Utility Logs
      v-flex(lg12)
        v-card
          v-toolbar(card color='white')
            v-text-field.hidden-sm-and-down(flat solo prepend-icon='search' placeholder='Search' v-model='search' hide-details)
            v-btn(icon @click="onReload")
              v-icon refresh
            v-btn(style="display:none" icon)
              v-icon filter_list
          v-divider
          v-card-text.pa-0
            v-data-table.elevation-1(
              :pagination.sync="pagination"
              disable-initial-sort=true
              :loading="loading"
              :headers='columns.headers' :search='search' :items='logs'
              :rows-per-page-items="[10,25,50,{text:'All','value':-1}]"
            )
              v-progress-linear.loading-progress(slot="progress" color="blue" indeterminate)
              template(slot='items' slot-scope='props')
                td.text-xs-left
                  v-chip(outline small label :color="logColor(props.item.level)") {{props.item.level}}
                td.name-column(@click="(e) => onCopyName(props.item.name, e)") {{ props.item.name }}
                td {{ props.item.duration }}
                td {{ props.item.startTimeStr }}
                td {{ props.item.endTimeStr }}
                td.long-text.pl-message(@click="onDialog(props.item.message, 'M')") {{ props.item.message }}
                td.long-text.pl-statement(@click="onDialog(props.item.statement, 'S')") {{ props.item.statement }}
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import clipboard from '@/util/clipboard'
import { codemirror } from 'vue-codemirror'

import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/base16-dark.css'
import 'codemirror/mode/javascript/javascript.js'

export default {
  components: {
    codemirror
  },
  data () {
    return {
      cmOptions: {
        // codemirror options
        tabSize: 4,
        mode: 'text/javascript',
        theme: 'base16-dark',
        lineNumbers: false,
        line: false,
        readOnly: true
      },
      name: undefined,
      loading: false,
      extDefVisible: false,
      search: '',
      selected: [],
      pagination: {
        sortBy: 'startTime',
        descending: true
      },
      columns: {
        headers: [
          {
            text: 'Level',
            value: 'level',
            align: 'left',
            width: '20px'
          },
          {
            text: 'Name',
            value: 'name'
          },
          {
            text: 'Duration',
            value: 'duration'
          },
          {
            text: 'Start Date',
            value: 'startTime'
          },
          {
            text: 'End Date',
            value: 'endTime'
          },
          {
            text: 'Message',
            value: 'message',
            class: "long-text"
          },
          {
            text: 'Statement',
            value: 'statement',
            class: "long-text"
          }
        ]
      },
      promptVisible: false,
      dialog: {
        content: undefined,
        visible: false
      }
    };
  },
  computed: {
    ...mapGetters('pl', [
      'logs'
    ])
  },
  methods: {
    ...mapActions('pl', [
      'findLogs'
    ]),
    onDialog(content, type) {
      this.dialog.content = content
      this.dialog.visible = true
    },
    logColor(level) {
      switch (level) {
        case 'INFO': return 'blue'
        case 'SUCCESS': return 'success'
        case 'ERROR': return 'error'
        case 'WARNING': return 'warning'
      }
    },
    ask(options) {
      this.prompt = options
      this.promptVisible = true
    },
    onPromptOk() {
      this.prompt.cb()
      this.promptVisible = false
    },
    onCopyName(name, e) {
      clipboard(name, e)
    },
    onCopyDialog(e) {
      clipboard(this.dialog.content, e)
    },
    onReload() {
      this.loading = true
      this.findLogs().then(_ => {
        this.loading = false
      }).catch(e => {
        console.log(e)
        this.$toast.error('Failed to fetch logs!')
        this.loading = false
      })
    }
  },
  mounted() {
    this.onReload()
  }
};
</script>

<style>
  /* #extractions tbody tr {
    border-left: 5px solid red !important;
  } */

  .hidden {
    visibility: hidden;
    transition: visibility 0ms;
  }

  tr:hover .hidden {
    visibility: visible;
  }
</style>

<style scoped>
  .v-progress-linear {
    height: 5px !important;
  }

  .long-text {
    max-width: 200px !important;
    white-space: nowrap !important;
    overflow: hidden !important;
    text-overflow: ellipsis !important;
  }
  .name-column {
    width: 400px;
    white-space: nowrap !important;
    overflow: hidden !important;
    text-overflow: ellipsis !important;
  }

  .pl-statement, .pl-message {
    color: #757575;
  }

  .pl-statement:hover,
  .pl-message:hover {
    cursor: pointer;
    color: #1b1b1b;
  }


</style>


