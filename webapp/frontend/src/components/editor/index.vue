<template lang="pug">
.editor
  textarea(ref='textarea')
</template>

<script>
import CodeMirror from 'codemirror'
import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/monokai.css'
import 'codemirror/mode/javascript/javascript'
import 'codemirror/mode/sql/sql'
require('codemirror/addon/display/autorefresh.js')
// require('codemirror/addon/selection/active-line.js')
require('codemirror/addon/selection/mark-selection.js')

export default {
  name: 'editor',
  data() {
    return {
      editor: false
    }
  },
  props: ['value', 'options'],
  watch: {
    value(value) {
      const editor_value = this.editor.getValue()
      if (value !== editor_value) {
        this.editor.setValue(this.value)
      }
    }
  },
  mounted() {
    this.editor = CodeMirror.fromTextArea(this.$refs.textarea, {
      lineNumbers: false,
      mode: 'text/x-sql',
      gutters: [],
      theme: 'monokai',
      autoRefresh: { delay: 250 },
      lint: false,
      ...this.options
    })

    this.editor.setValue(this.value)
    this.editor.on('change', cm => {
      this.$emit('changed', cm.getValue())
      this.$emit('input', cm.getValue())
    })
  },
  methods: {
    getValue() {
      return this.editor.getValue()
    }
  }
}
</script>

<style scoped>
.editor{
  height: 100%;
  position: relative;
}
.editor >>> .CodeMirror {
  height: 400px;
  min-height: 300px;
}
.editor >>> .CodeMirror-scroll{
  min-height: 300px;
}
.editor >>> .cm-s-monokai span.cm-string {
  color: #F08047;
}
</style>
