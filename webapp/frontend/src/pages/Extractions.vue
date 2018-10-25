<template lang="pug">
#extractions
  v-dialog(v-model='promptVisible' width='500')
    v-card
      v-card-title.headline(primary-title='')
        | {{ prompt.title }}
      v-alert(v-if="prompt.warning" :value="true" type="warning") Task is already running!
      v-card-text
        | {{ prompt.text }}
      v-divider
      v-card-actions
        v-spacer
        v-btn(color='primary' flat='' @click='onPromptOk')
          | OK
  extraction-definition(v-if="extDefVisible" :visible="extDefVisible" @close="onCloseExtDef" :name="name")
  create-table(v-if="createTableVisible" :visible="createTableVisible" @close="onCloseCreateTable")
  v-container(grid-list-xl fluid)
    v-layout(v-show="false" align-center justify-center row fill-height)
      v-btn(large color="success") Create Map
        v-icon(right dark) add
    v-layout(v-show="true" row wrap)
      v-flex(sm12)
        h3 Extraction Tables
      v-flex(lg12)
        v-card
          v-toolbar(card color='white')
            v-text-field.hidden-sm-and-down(flat solo prepend-icon='search' placeholder='Search' v-model='search' hide-details)
            v-tooltip(top)
              v-btn(v-if="selected.length > 0" slot="activator" icon @click="onExcludeSelected()")
                v-icon(color) not_interested
              span Exclude Extraction
            v-tooltip(top)
              v-btn(slot="activator" v-if="selected.length > 0" icon @click="onExcludeSelected(false)")
                v-icon(color) done
              span Include Extraction
            v-btn(v-if="selected.length > 0" icon)
              v-icon(color='pink') delete
            v-tooltip(top)
              v-btn(icon slot="activator" @click="extDefVisible=true")
                v-icon(color='primary') add
              span Create New Extraction
            v-tooltip(top)
              v-btn(icon @click="onReload" slot="activator")
                v-icon refresh
              span Reload
            v-btn(outline round color="success" @click="createTableVisible = true") Create Table
          v-divider
          v-card-text.pa-0
            v-data-table.elevation-1(
              :pagination.sync="pagination"
              :loading="loading"
              :headers='columns.headers' :search='search' :items='maps'
              :rows-per-page-items="[10,25,50,{text:'All','value':-1}]"
              item-key='name' select-all
              v-model='selected'
            )
              v-progress-linear.loading-progress(slot="progress" color="blue" indeterminate)
              template(slot='items' slot-scope='props')
                td(:class="props.item.status==='RUNNING' ? 'running-ext' : ''")
                  v-checkbox(primary hide-details v-model='props.selected')
                td.text-xs-left
                  v-chip(v-if="props.item.excluded" outline small label color="error") EXCLUDED
                td(@click="(e) => onCopyName(props.item.name, e)") {{ props.item.name }}
                td {{ props.item.dbLink }}
                td {{ props.item.sourceSchema }}.{{ props.item.sourceTable }}
                td {{ props.item.targetSchema }}.{{ props.item.targetTable }}
                td.text-xs-right.col-action(px-0)
                  v-tooltip(top)
                    v-icon.action.hidden.mr-2(slot="activator" color='primary' small @click="onRun(props.item)") play_arrow
                    span Run Extraction
                  v-tooltip(top)
                    v-icon.action.hidden.mr-2(slot="activator" color='primary' small @click="onEdit(props.item.name)") edit
                    span Edit
                  v-tooltip(top)
                    v-icon.action.hidden.mr-2(slot="activator" color='pink' small @click="onDelete(props.item.name)") delete
                    span Delete Extraction
                  v-tooltip(top)
                    v-icon.action.hidden.mr-2(slot="activator" color small @click="onExclude(props.item.name)") not_interested
                    span Exclude Extraction
                  v-tooltip(top)
                    v-icon.action.hidden(slot="activator" color small @click="onExclude(props.item.name, false)") done
                    span Include Extraction
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import ExtractionDefinition from '@/components/extdef'
import CreateTable from '@/components/ctas'
import clipboard from '@/util/clipboard'

export default {
  components: {
    ExtractionDefinition,
    CreateTable
  },
  data () {
    return {
      name: undefined,
      loading: false,
      extDefVisible: false,
      createTableVisible: false,
      search: '',
      selected: [],
      pagination: {
        sortBy: 'name',
        descending: false
      },
      columns: {
        headers: [
          {
            text: '',
            value: 'excluded',
            align: 'left',
            width: '20px'
          },
          {
            text: 'Name',
            value: 'name'
          },
          {
            text: 'DB Link',
            value: 'dblink'
          },
          {
            text: 'Source Table',
            value: 'sourceTable'
          },
          {
            text: 'Target Table',
            value: 'targetTable'
          },
          {
            text: '',
            value: ''
          }
        ]
      },
      promptVisible: false,
      prompt: {
        warning: false,
        title: undefined,
        text: undefined,
        cb: undefined
      }
    };
  },
  computed: {
    ...mapGetters('elo', [
      'maps'
    ])
  },
  methods: {
    ...mapActions('elo', [
      'excludeMap',
      'includeMap',
      'excludeMaps',
      'includeMaps',
      'findMaps',
      'runMap',
      'deleteMap'
    ]),
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
    onReload() {
      this.loading = true
      this.findMaps().then(_ => {
        this.loading = false
      }).catch(e => {
        console.log(e)
        this.loading = false
      })
    },
    onExclude(name, excluded = true) {
      if (excluded) {
        this.excludeMap(name).then(_=> {
          this.$toast('Excluded extraction')
        }, error => {
          console.log(error)
        })
      } else {
        this.includeMap(name).then(_=> {
          this.$toast('Included selected extraction')
        }).catch(e => {
          console.log(error)
          this.$toast('Failed to include extraction', {color: 'error'})
        })
      }
    },
    onExcludeSelected(excluded = true) {
      const names = _.map(this.selected , s => s.name)
      if (excluded) {
        this.excludeMaps(names).then(result => {
          this.$toast('Excluded selected extractions')
        }).catch(error => {
          console.log(error)
        })
      } else {
        this.includeMaps(names).then(_=> {
          this.$toast('Included selected extractions')
        }, error => {
          console.log(error)
        })
      }
    },
    onEdit(name) {
      this.name = name
      this.extDefVisible = true
    },
    onCloseExtDef() {
      this.extDefVisible = false
      this.name = undefined
    },
    onCloseCreateTable() {
      this.createTableVisible = false
    },
    onRun(map) {
      const name = map.name

      if (map.excluded) {
        this.$toast.warning(`${name} is excluded! Will not run.`)
        return
      }

      const prompt = {
        title: `Run ${name} ?`,
        text: `This will run extraction process for  ${name}`,
        warning: map.status === 'RUNNING'
      }
      prompt.cb = () => {
        this.runMap(name).then(response => {
          this.$toast.success(`Started extraction process for ${name}`)
          if (map) {
            map.status = 'RUNNING'
          }
        }, error => {
          console.log(error)
          this.$toast.error(`Failed to start extraction for ${name}`)
        })
      }
      this.ask(prompt)
    },
    onDelete(name) {
      const prompt = {
        title: `Delete ${name} ?`,
        text: `This delete extraction definition for  ${name}`
      }
      prompt.cb = () => {
        this.deleteMap(name).then(map => {
          this.$toast(`Deleted ${name}`, {color: 'success'})
        }, error => {
          console.log(error)
          this.$toast(`Failed to delete ${name}`, {color: 'success'})
        })
      }
      this.ask(prompt)
    }
  },
  mounted() {
    if (_.isEmpty(this.maps)) {
      this.onReload()
    }
  }
};
</script>

<style>
  /* #extractions tbody tr {
    border-left: 5px solid red !important;
  } */

  .col-action .v-icon {
    transition: 0ms !important;
  }

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

  .running-ext {
    border-left: 5px solid cornflowerblue !important;
  }

</style>


