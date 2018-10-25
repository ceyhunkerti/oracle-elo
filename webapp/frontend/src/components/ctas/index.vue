<template lang="pug">
v-dialog(v-model='visible' fullscreen hide-overlay transition='dialog-bottom-transition' scrollable)
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
        v-btn(dark flat @click.native='onSave' :disabled="!isValid || saveDisabled") Save
    v-progress-linear.main-progress(v-show="progressVisible" color="error" :indeterminate="true")
    v-alert.top-alert(
      :value="alert.visible"
      :color="alert.color"
      :icon="alert.icon"
      outline) {{ alert.text }}
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
                div(v-if="showTableDef")
                  v-layout(column fill-height)
                    v-flex(xs2)
                      h5.secondary-header Database Link
                    v-flex.mt-1
                      v-autocomplete(:items='dblinks' label='DB Link' solo v-model="ctas.dbLink" :rules="[rules.required]")
                  v-layout(row wrap align-center justify-space-between fill-height)
                    v-flex(xs1)
                      h5.secondary-header Source
                  v-layout.mt-1(align-center justify-space-between row fill-height)
                    v-flex(xs6 style="margin-right:5px;")
                      v-autocomplete(:items='sourceSchemas' label='Source Schema' solo v-model="ctas.sourceSchema" :rules="[rules.required]")
                    v-flex(xs6 style="margin-left:5px;")
                      v-autocomplete(:items='sourceTables' label='Source Table' solo v-model="ctas.sourceTable" :rules="[rules.required]")
                  v-layout(row wrap fill-height)
                    v-flex(xs1)
                      h5.secondary-header Target
                  v-layout.mt-1(justify-space-between column fill-height full-width)
                    v-flex
                      v-autocomplete(:items='targetSchemas' :rules="[rules.required]" label='Target Schema' solo v-model="ctas.owner" )
                    v-flex
                      v-text-field(clearable :rules="[rules.required]"  label='Table name' solo v-model="ctas.name")
                  v-layout(column wrap fill-height)
                    v-flex
                      h5.secondary-header Options
                    v-flex.mt-1
                      v-text-field(label='Create options. eg: parallel nologging compress etc.' solo v-model="ctas.options" )
                v-flex.mt-3(sm12)
                  h4 Columns
                v-layout.mt-3(row wrap)
                  v-flex(sm12)
                    v-card
                      v-toolbar(card color='white')
                        v-text-field.hidden-sm-and-down(flat solo prepend-icon='search' placeholder='Search' v-model='search' hide-details)
                        v-btn(v-if="selected.length > 0" icon @click="onSelectColumn(selected)")
                          v-icon(color='success') done
                        v-btn(v-if="selected.length > 0" icon @click="onSelectColumn(selected, false)")
                          v-icon(color='error') not_interested
                      v-divider
                      v-card-text.pa-0(style="width:100%")
                        v-data-table.elevation-1(
                          :pagination.sync="pagination"
                          :headers='columns.headers'
                          :search='search' :items='ctas.columns'
                          :rows-per-page-items="[10,25,50,{text:'All','value':-1}]"
                          item-key='name' select-all v-model='selected'
                        )
                          template(slot='items' slot-scope='props')
                            td(width=20)
                              v-checkbox(primary hide-details v-model='props.selected')
                            td.text-xs-left
                              v-chip(v-if="props.item.selected" outline small label color="success") Selected
                            td
                              | {{ props.item.name }}
                            td(class="justify-center layout px-0")
                              v-tooltip(top)
                                v-icon.action.hidden.mr-2(slot="activator" color="success" small @click="onSelectColumn(props.item.name)" ) done
                                span Select Column
                              v-tooltip(top)
                                v-icon.action.hidden.mr-2(slot="activator" color="error" small @click="onSelectColumn(props.item.name, false)" ) not_interested
                                span Not Interested
</template>


<script>
import { mapActions } from 'vuex'
import VuePerfectScrollbar from 'vue-perfect-scrollbar'
import uuidv1 from 'uuid/v1'
import _ from 'lodash'

export default {
  props: {
    visible: { type: Boolean, default: false }
  },
  components: {
    VuePerfectScrollbar
  },
  data() {
    return {
      rules: {
        required: value => !!value || 'Required.'
      },
      saveDisabled: false,
      progressVisible: false,
      alert: {
        visible: false,
        text: 'warning',
        icon: 'info'
      },
      pagination: {
        sortBy: 'name'
      },
      ctas: {
        owner: undefined,
        name: undefined,
        dbLink: undefined,
        sourceSchema: undefined,
        sourceTable: undefined,
        columns: [],
        options: undefined
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
          const m = _.find(this.all, { id })
          if (!m) { return }
          const i = _.findIndex(this.all, { id })
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
      showTableDef: true,
      dblinks: [],
      sourceSchemas: [],
      sourceTables: [],
      targetSchemas: [],
      search: '',
      selected: [],
      columns: {
        headers: [
          {
            text: 'Selected',
            value: 'selected',
            align: 'left',
            width: '20px'
          },
          {
            text: 'Column Name',
            value: 'name'
          },
          {
            text: '',
            sortable: false
          }
        ]
      }
    }
  },
  computed: {
    title() {
      return  `Create Table`
    },
    isValid() {
      return (
        this.ctas.owner &&
        this.ctas.name &&
        !_.chain(this.ctas.columns)
          .filter(c => c.selected)
          .isEmpty(this.ctas.columns)
          .value() &&
        this.ctas.dbLink &&
        this.ctas.sourceSchema &&
        this.ctas.sourceTable
      )
    }
  },
  watch: {
    visible(v) {
    },
    'ctas.dbLink'() {
      if (this.visible) {
        this.loadSourceSchemas()
      }
    },
    'ctas.sourceSchema'() {
      if (this.visible) {
        this.loadSourceTables()
      }
    },
    'ctas.sourceTable'(v) {
      if (this.visible) {
        const dbLink = this.ctas.dbLink
        const owner = this.ctas.sourceSchema
        const name = this.ctas.sourceTable
        this.progressVisible = true
        this.findSourceColumns({ dbLink, owner, name }).then(response => {
          this.progressVisible = false
          this.ctas.columns = _.map(response.data, c => {
            const selected = false
            const name = c
            return { selected, name }
          })
        }, error => {
          this.progressVisible = false
          console.log(error)
          this.$toast.error('Failed to find columns!')
        })
      }
      if (v && !this.ctas.owner) {
        this.ctas.name = v
      }
    },
  },
  methods: {
    ...mapActions('elo', [
      'findDbLinks',
      'findSourceSchemas',
      'findSourceTables',
      'findSourceColumns',
      'findTargetSchemas'
    ]),
    ...mapActions('meta', [
      'createTable'
    ]),
    progress(show = true) {
      this.progressVisible = show
      this.saveDisabled = show
    },
    clear() {
      this.ctas = {
        columns: []
      }
    },
    init() {
      const el = document.documentElement
      el.classList.add('overflow-hidden')

      if (!_.isEmpty(this.name)) {
        this.findMap(this.name).then(response => {
          this.elo = response.data
        }).catch(e => {
          console.log(e)
          this.$toast(`Unable to find map ${this.name}`)
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
      this.progress()
      this.findDbLinks().then(response => {
        this.progress(false)
        this.dblinks = response.data.sort()
        this.notification.remove(id)
      }, error => {
        this.progress(false)
        this.notification.remove(id)
      })
    },
    loadSourceSchemas() {
      this.progressVisible = true
      const dbLink = this.ctas.dbLink
      this.progress()
      this.findSourceSchemas({ dbLink }).then(response => {
        this.progress(false)
        this.sourceSchemas = response.data.sort()
      }, error => {
        this.progress(false)
        this.$toast.error('Failed to find source schemas!')
        console.log(error)
      })
    },
    loadSourceTables() {
      const dbLink = this.ctas.dbLink
      const owner = this.ctas.sourceSchema
      this.progress()
      this.findSourceTables({ dbLink, owner }).then(response => {
        this.progress(false)
        this.sourceTables = response.data.sort()
      }, error => {
        this.progress(false)
        this.$toast.error('Failed to find source tables!')
        console.log(error)
      })
    },
    loadTargetSchemas() {
      const id = this.message('Loading target schemas ...')
      this.progress()
      this.findTargetSchemas().then(response => {
        this.progress(false)
        this.targetSchemas = response.data.sort()
        this.notification.remove(id)
      }, error => {
        this.progress(false)
        this.notification.remove(id)
        console.log(error)
      })
    },
    onClose() {
      const el = document.documentElement
      el.classList.remove('overflow-hidden')
      this.$emit('close')
    },
    onSelectColumn(n, selected = true) {
      const names = _.isArray(n) ? _.map(n, 'name') : [n]
      _.each(names, name => {
        const index = _.findIndex(this.ctas.columns, { name })
        if (index !== -1) {
          this.ctas.columns.splice(index, 1, { name, selected })
        }
      })
    },
    onSave() {
      let data = { ...this.ctas }
      data.columns = _.chain(this.ctas.columns).filter(c => c.selected).map('name').value()
      this.progress()
      this.createTable(data).then(response => {
        this.progress(false)
        const table = response.data
        this.$emit('close')
        this.$toast.success(`Created ${table.owner}.${table.name}`)
      }, error => {
        console.log(error)
        this.progress(false)
        this.$toast.success(`Failed to create ${data.owner}.${data.name}`)
      })
    }
  },
  mounted() {
    this.init()
  }
}
</script>

<style>
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



