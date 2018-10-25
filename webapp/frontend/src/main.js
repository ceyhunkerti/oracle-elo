// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import Vuetify from 'vuetify'
import router from './router'
import store from './store'
import 'font-awesome/css/font-awesome.css'
import './theme/default.styl'
import VeeValidate from 'vee-validate'
import colors from 'vuetify/es5/util/colors'
import Truncate from 'lodash.truncate'
// import SnackbarStackPlugin from 'snackbarstack'
import './permission' // permission control
import VuetifyToast from 'vuetify-toast-snackbar'
import Cookies from 'js-cookie'

Vue.config.productionTip = false
// Helpers
// Global filters
Vue.filter('truncate', Truncate)
Vue.use(VeeValidate, { fieldsBagName: 'formFields' })
Vue.use(Vuetify, {
  theme: {
    primary: colors.blue.base,
    secondary: colors.blue.lighten4,
    accent: colors.blue.base
  },
  options: {
    themeVariations: ['primary', 'secondary', 'accent'],
    extra: {
      mainToolbar: {
        color: 'primary'
      },
      sideToolbar: {
      },
      sideNav: 'primary',
      mainNav: 'primary lighten-1',
      bodyBg: ''
    }
  }
})

Vue.use(VuetifyToast, {
  x: 'right', // default
  y: 'bottom', // default
  color: 'info', // default
  icon: 'info',
  timeout: 3000, // default
  dismissable: true, // default
  autoHeight: false, // default
  multiLine: false, // default
  vertical: false, // default
  shorts: {
    custom: 'purple'
  },
  property: '$toast' // default
})

// Bootstrap application components
// Vue.use(SnackbarStackPlugin)

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  components: { App },
  template: '<App/>',
  mounted() {
    const val = Cookies.get('theme-primary')
    const base = colors[val] ? colors[val].base : colors['blue']
    this.$vuetify.theme.primary = base
    this.$vuetify.dark = (Cookies.get('theme-side') === 'dark')
    this.$store.dispatch('init', { root: true })
  }
})
