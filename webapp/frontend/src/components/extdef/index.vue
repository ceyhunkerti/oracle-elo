<template lang="pug">
v-dialog.ext-def(v-model='visible' fullscreen='' hide-overlay='' transition='dialog-bottom-transition' scrollable lazy)
  v-snackbar(
    v-model="notification.visible"
    :color="notification.color"
    :timeout="notification.timeout"
  )
    | {{notification.text}}
  v-card(tile='')
    v-toolbar(card='' dark='' color='primary')
      v-btn(icon='' dark='' @click.native='onClose')
        v-icon close
      v-toolbar-title {{title}}
      v-spacer
      v-toolbar-items
        v-btn(dark flat @click.native='onSave' :disabled="!isValid || p.saveDisabled") Save
    v-progress-linear.main-progress(v-show="p.visible" color="error" :indeterminate="true")
    v-card-text
      v-container(align-center)
        v-layout(row wrap )
          v-flex
            vue-perfect-scrollbar(class="mail-list--scrollbar")
              v-container
                v-layout(row wrap style="margin-bottom:10px;" align-center justify-space-between fill-height)
                  v-flex(xs1)
                    h4 Definition
                  v-flex
                    v-switch(label='' v-model='showTableDef')
                v-layout(column fill-height)
                  v-flex(xs2)
                    h5.secondary-header Name
                  v-flex.mt-1()
                    v-text-field(label='Extraction name' solo v-model="elo.name" :disabled="name !== undefined")
                div(v-if="showTableDef")
                  v-layout(column fill-height)
                    v-flex(xs2)
                      h5.secondary-header Database Link
                    v-flex.mt-1
                      v-autocomplete(:items='dblinks' label='DB Link' solo v-model="elo.dbLink")
                  v-layout(row wrap align-center justify-space-between fill-height)
                    v-flex(xs1)
                      h5.secondary-header Source
                  v-layout.mt-1(align-center justify-space-between row fill-height)
                    v-flex(xs6 style="margin-right:5px;")
                      v-autocomplete(:items='sourceSchemas' label='Source Schema' solo v-model="elo.sourceSchema")
                    v-flex(xs6 style="margin-left:5px;")
                      v-autocomplete(:items='sourceTables' label='Source Table' solo v-model="elo.sourceTable")
                  v-layout(row wrap fill-height)
                    v-flex(xs1)
                      h5.secondary-header Target
                  v-layout.mt-1(align-center justify-space-between row fill-height)
                    v-flex.mr-2(xs6)
                      v-autocomplete(:items='targetSchemas' label='Target Schema' solo v-model="elo.targetSchema" )
                    v-flex(xs6 )
                      v-combobox(ref="targetTable" :items='targetTables' label='Target Table' solo v-model="elo.targetTable")
                  v-layout(row wrap fill-height)
                    v-flex(xs1)
                      h5.secondary-header Hint
                  v-layout.mt-1(align-center justify-space-between row fill-height)
                    v-flex.mr-2(xs6)
                      v-text-field(label='Source Hint' solo v-model="elo.sourceHint")
                    v-flex(xs6)
                      v-text-field(label='Target Hint' solo v-model="elo.targetHint")

                  v-layout.mt-3(align-center justify-space-between row fill-height)
                    v-flex
                      v-switch(label='Delta' v-model='isDelta')
                  v-layout.mt-3(xs6 v-if="isDelta" align-center justify-space-between row fill-height)
                    v-flex.mr-2(xs6)
                      v-autocomplete(
                        :items='elo.columns' label='Delta Column' solo
                        item-value="target"
                        item-text="target"
                        v-model="elo.deltaColumn"
                      )
                    v-flex(xs6 )
                      v-text-field(label='Last Delta' solo v-model="elo.lastDelta")
                  v-layout.mt-3(row fill-height)
                    v-flex(xs3)
                      v-checkbox(label='Excluded' v-model='elo.excluded')
                    v-flex(xs3)
                      v-checkbox(label='Load with drop/create' v-model='elo.dropCreate')
                    v-flex(xs3)
                      v-checkbox(label='Analyze' v-model='elo.analyze')
                    v-layout.mt-3(row fill-height)
                      v-input
                v-flex.mt-3(sm12)
                  h4 Columns
                v-layout.mt-3(row wrap)
                  v-flex(sm12)
                    v-card
                      v-toolbar(card color='white')
                        v-text-field.hidden-sm-and-down(flat solo prepend-icon='search' placeholder='Search' v-model='search' hide-details)
                        v-tooltip(top)
                          v-btn(v-if="selected.length > 0" slot="activator" icon @click="onExcludeColumn(selected, false)")
                            v-icon(color) done
                          span Include Selected
                        v-tooltip(top)
                          v-btn(v-if="selected.length > 0" slot="activator" icon @click="onExcludeColumn(selected)")
                            v-icon(color) not_interested
                          span Exclude Selected
                        v-tooltip(top)
                          v-btn(v-if="selected.length > 0" slot="activator" icon @click="onMapColumn(selected, false)")
                            v-icon(color='pink') delete
                          span Delete Selected Column Mappings
                        v-tooltip(top)
                          v-btn(v-if="selected.length > 0" slot="activator" icon @click="onMapColumn(selected)")
                            v-icon(color='success') add
                          span Add Selected Column Mappings
                        v-tooltip(v-if="displayWarning" top)
                          v-btn(icon slot="activator" @click="onWarning")
                            v-icon(color="error") notification_important
                          span Important Warning
                        v-tooltip(top)
                          v-btn(icon slot="activator" @click="onReloadColumns")
                            v-icon refresh
                          span Reload
                      v-divider
                      v-card-text.pa-0(style="width:100%")
                        v-data-table.elevation-1(
                          :pagination.sync="pagination"
                          :headers='columns.headers'
                          :search='search' :items='elo.columns'
                          :rows-per-page-items="[10,25,50,{text:'All','value':-1}]"
                          item-key='id' select-all v-model='selected'
                        )
                          template(slot='items' slot-scope='props')
                            td(width=20)
                              v-checkbox(primary hide-details v-model='props.selected')
                            td()
                              v-layout(row)
                                v-tooltip(top)
                                  v-chip(slot="activator" v-if="!props.item.valid" outline small label color="pink" ) Invalid
                                  span Column does not exist in target
                                v-tooltip(top)
                                  v-chip(slot="activator" v-if="!props.item.mapped" outline small label color="pink" ) Not Mapped
                                  span Not mapped to any value
                                v-tooltip(top)
                                  v-chip(slot="activator" v-if="props.item.mapped && props.item.excluded === true" outline small label color="gray" ) Excluded
                                  span Excluded from loading process
                            td
                              | {{ props.item.target }}
                            td
                              v-edit-dialog(:return-value.sync='props.item.expression' lazy)
                                | {{ props.item.expression }}
                                v-text-field(
                                  slot='input'
                                  v-model='props.item.expression'
                                  label='Edit' single-line
                                  :disabled="!props.item.mapped")

                            td.text-xs-right.col-action(px-0)
                              v-tooltip(top)
                                v-icon.action.hidden.mr-2(slot="activator" color small @click="onExcludeColumn(props.item.id, false)") done
                                span Include Column Mapping
                              v-tooltip(top)
                                v-icon.action.hidden.mr-2(slot="activator" color small @click="onExcludeColumn(props.item.id)") not_interested
                                span Include Column Mapping
                              v-tooltip(top)
                                v-icon.action.hidden.mr-2(slot="activator" color='pink' small @click="() => { onMapColumn(props.item.id, false) }") delete
                                span Delete Column Mapping
                              v-tooltip(top)
                                v-icon.action.hidden.mr-2(slot="activator" color='success' small @click="() => { onMapColumn(props.item.id, true) }") add
                                span Add Column Mapping
</template>


<script>
import { mapActions } from 'vuex'
import VuePerfectScrollbar from 'vue-perfect-scrollbar'
import uuidv1 from 'uuid/v1'
import _ from 'lodash'

export default {
  props: {
    name: { type: String, default: undefined},
    visible: { type: Boolean, default: false },
  },
  components: {
    VuePerfectScrollbar
  },
  data() {
    return {
      pagination: {
        sortBy: 'name'
      },
      p: {
        visible: false,
        saveDisabled: false
      },
      elo: {
        name: undefined,
        dbLink: undefined,
        sourceSchema: undefined,
        sourceTable: undefined,
        targetSchema: undefined,
        targetTable: undefined,
        deltaColumn: undefined,
        lastDelta: undefined,
        analyze: false,
        columns: [],
        _columns: []
      },
      notification: {
        id: undefined,
        visible: false,
        text: '',
        color: '',
        timeout: 0,
        all: [],
        add(message) {
          this.all.push(message)
          if (!this.visible) {
            this.next()
          }
        },
        remove(id) {
          const i = _.findIndex(this.all, { id })
          if (i === -1) { return }
          const m = this.all[i]
          this.all.splice(i, 1)
          if (m.id === this.id) {
            this.visible = false
            this.next()
          }
        },
        next() {
          if (this.all.length > 0) {
            this.id = this.all[0].id
            this.text = this.all[0].message
            this.color = this.all[0].color
            this.visible = true
            this.timeout = this.all[0].timeout || 0
          }
        }
      },
      isDelta: false,
      showTableDef: true,
      dblinks: [],
      sourceSchemas: [],
      sourceTables: [],
      targetSchemas: [],
      targetTables: [],
      search: '',
      selected: [],
      columns: {
        headers: [
          {
            text: '',
            value: 'excluded',
            align: 'center',
            width: '20px'
          },
          {
            text: 'Target Column',
            value: 'sourceTable'
          },
          {
            text: 'Expression',
            value: 'targetTable'
          },
          {
            text: '',
            value: 'name',
            sortable: false
          }
        ]
      }
    }
  },
  computed: {
    title() {
      return  this.name ? `Update Extraction Definition` : `New Extraction Definition`
    },
    isValid() {
      return (
        this.elo.name &&
        this.elo.dbLink &&
        this.elo.sourceSchema &&
        this.elo.sourceTable &&
        this.elo.targetSchema &&
        this.elo.targetTable &&
        !_.chain(this.elo.columns)
          .filter(c => c.mapped && !c.excluded)
          .isEmpty()
          .value()
      )
    },
    displayWarning() {
      const invalids = _.filter(this.elo.columns, c => c.valid === false)
      const includeds = _.filter(this.elo.columns, c => !c.excluded)
      return !_.isEmpty(this.elo.columns) && (_.isEmpty(includeds) || !_.isEmpty(invalids))
    }
  },
  watch: {
    isDelta(v) {
      if (!this.isDelta) {
        this.elo.deltaColumn = undefined
        this.elo.lastDelta = undefined
      }
    },
    'notification.all'(v) {
      const b = !_.isEmpty(v)
      this.p.visible = b
      this.saveDisabled = b
    },
    'elo.dbLink'() {
      if(this.visible) {
        this.loadSourceSchemas()
      }
    },
    'elo.sourceSchema'() {
      if(this.visible) {
        this.loadSourceTables()
      }
    },
    'elo.sourceTable'(v) {
      if (!this.name && !this.elo.name) {
        this.elo.name = v
      }
    },
    'elo.targetSchema'() {
      if (this.visible) {
        this.loadTargetTables()
      }
    },
    'elo.targetTable'() {
      if (this.visible && !this.elo.create) {
        this.loadTargetColumns()
      }
    }
  },
  methods: {
    ...mapActions('elo', [
      'findDbLinks',
      'findSourceSchemas',
      'findSourceTables',
      'findTargetSchemas',
      'findTargetTables',
      'findTargetColumns',
      'createMap',
      'updateMap',
      'findMap'
    ]),
    onWarning() {
      const invalids = _.filter(this.elo.columns, c => c.valid === true)
      const includeds = _.filter(this.elo.columns, c => !c.excluded)
      if (!_.isEmpty(invalids)) {
        this.$toast.warning('You have invalid column mappings!')
        return
      }
      if (_.isEmpty(included)) {
        this.$toast.warning('All columns are excluded')
        return
      }
    },
    clear() {
      this.elo = {
        columns: []
      }
    },
    init() {
      const el = document.documentElement
      el.classList.add('overflow-hidden')
      if (!_.isEmpty(this.name)) {
        const id = this.message('Finding map ...')
        this.findMap(this.name).then(response => {
          this.notification.remove(id)
          const elo = response.data
          elo.columns = _.map(elo.columns, column => {
            column.mapped = true
            column.valid = true
            return column
          })
          this.elo = elo
          this.elo._columns = _.cloneDeep(elo.columns)
        }).catch(e => {
          console.log(e)
          this.notification.remove(id)
          this.$toast.error(`Unable to find map ${this.name}`)
        })
      }
      this.loadDbLinks()
      this.loadTargetSchemas()
    },
    getColumnMapClass(mapped = true) {
      if (mapped) { return }
      return "unmapped-column"
    },
    message(message, options) {
      const id = uuidv1()
      const m = { id, message, ...options }
      this.notification.add(m)
      return id
    },
    loadDbLinks() {
      const id = this.message('Loading db links ...')
      this.findDbLinks().then(response => {
        this.dblinks = response.data.sort()
        this.notification.remove(id)
      }, error => {
        this.notification.remove(id)
      })
    },
    loadSourceSchemas() {
      const dbLink = this.elo.dbLink
      const id = this.message('Loading source schemas ...')
      this.findSourceSchemas({ dbLink }).then(response => {
        this.notification.remove(id)
        this.sourceSchemas = response.data.sort()
      }, error => {
        this.notification.remove(id)
        console.log(error)
      })
    },
    loadSourceTables() {
      const dbLink = this.elo.dbLink
      const owner = this.elo.sourceSchema
      const id = this.message('Finding source tables ...')
      this.findSourceTables({ dbLink, owner }).then(response => {
        this.notification.remove(id)
        this.sourceTables = response.data.sort()
      }, error => {
        this.notification.remove(id)
        console.log(error)
      })
    },
    loadTargetSchemas() {
      const id = this.message('Loading target schemas ...')
      this.findTargetSchemas().then(response => {
        this.targetSchemas = response.data.sort()
        this.notification.remove(id)
      }, error => {
        this.notification.remove(id)
        console.log(error)
      })
    },
    loadTargetTables() {
      const id = this.message('Loading target tables ...')
      const owner = this.elo.targetSchema
      this.findTargetTables({ owner }).then(response => {
        this.notification.remove(id)
        this.targetTables = response.data.sort()
      }, error => {
        this.notification.remove(id)
        console.log(error)
      })
    },
    mergeWithMapColumns(columns) {
      // mark invalid mappings. column map exists but column not in target table
      _.chain(this.elo.columns)
        .map(c => c.target.toUpperCase())
        .difference(columns)
        .each(target => {
          let c = _.find(this.elo.columns, { target })
          c.valid = false
        }).value()

      // mark not mapped columns
      _.chain(columns)
        .difference(_.map(this.elo.columns, c => c.target.toUpperCase()))
        .each(target => {
          const mapped = false
          const expression = target
          const excluded = false
          const valid = true
          this.elo.columns.push({excluded, target, expression, valid, mapped })
        }).value()

      _.each(this.elo.columns, column => {
        column.id = uuidv1()
      })

    },
    loadTargetColumns() {
      const id = this.message('Loading columns ...')
      const owner = this.elo.targetSchema
      const table = this.elo.targetTable
      this.findTargetColumns({ owner, table }).then(response => {
        this.mergeWithMapColumns(_.map(response.data, c => c.toUpperCase()))
        this.notification.remove(id)
      }, error => {
        this.notification.remove(id)
        console.log(error)
      })
    },
    onReloadColumns() {
      this.elo.columns = _.cloneDeep(this.elo._columns)
      console.log(this.elo.columns)
      this.loadTargetColumns()
    },
    onClose() {
      const el = document.documentElement
      el.classList.remove('overflow-hidden')
      this.$emit('close')
    },
    onSave() {
      const elo = this.elo
      elo.columns = _.filter(this.elo.columns, c => c.mapped)
      if (_.isEmpty(elo.columns)) {
        this.$toast.warning('You must map at least one column!')
        return
      }

      if (this.name) {
        const id = this.message('Updating mapping ...')
        this.updateMap(elo).then(response => {
          this.notification.remove(id)
          this.$toast('Extraction definition saved', {color: 'success'})
          this.$emit('close')
        }).catch(e => {
          this.notification.remove(id)
          const message = e.response.data.message
          this.$toast.error(message)
        })
      } else {
        const id = this.message('Creating mapping ...')
        this.createMap(elo).then(response => {
          this.notification.remove(id)
          this.$toast('Extraction definition saved', {color: 'success'})
          this.$emit('close')
        }).catch(e => {
          this.notification.remove(id)
          const message = error.response.data.message
          this.$toast.error(message)
        })
      }
    },
    onExcludeColumn(c, exclude = true) {
      _.each((_.isArray(c) ? _.map(c, 'id') : [c]), id => {
        const index = _.findIndex(this.elo.columns, { id })
        const record = _.chain(this.elo.columns).find({ id }).cloneDeep().value()
        record.excluded = exclude
        this.elo.columns.splice(index, 1, record)
      })
      if (exclude && this.selected.length === this.elo.columns.length) {
        this.$toast.warning('Excluding all columns will disable extraction')
      }
    },
    onMapColumn(c, map = true) {
      _.each((_.isArray(c) ? _.map(c, 'id') : [c]), id => {
        const index = _.findIndex(this.elo.columns, { id })
        const record = _.chain(this.elo.columns).find({ id }).cloneDeep().value()
        record.mapped = map
        this.elo.columns.splice(index, 1, record)
      })
    }
  },
  mounted() {
    this.init()
  }
}
</script>

<style>

.col-action .v-icon {
  transition: 0ms !important;
}


tr:hover td i.action  {
  display: inline-flex !important;
  cursor: pointer;
  opacity: 0.6;
}

tr:hover td i.action:hover {
  opacity: 1;
}

.secondary-header {
  color: #9e9e9e;
}

.main-progress {
  margin: 0px;
  height: 5px !important;
}

.overflow-hidden {
  overflow-y: hidden;
}

.unmapped-column a,
.unmapped-column {
  color: red !important;
  opacity: 0.6;
}

.ext-def .v-progress-linear {
  margin: 0px !important;
}

</style>

<style >
.v-alert.v-alert {
  width:100% !important;
  margin-top:0 !important;
  border-left: 0 !important;
  border-top: 0 !important;
  border-right: 0 !important;
}
</style>



