<template lang="pug">
#appRoot
  template(v-if='!$route.meta.public')
    v-app#inspire.app
      app-drawer.app--drawer
      app-toolbar.app--toolbar(@themeSettings="openThemeSettings")
      v-content
        // Page Header
        page-header(v-if='$route.meta.breadcrumb')
        .page-wrapper
          router-view
        // App Footer
        v-footer.white.pa-3.app--footer(height='auto')
          span.caption blue &copy; {{ new Date().getFullYear() }}
          v-spacer
          span.caption.mr-1  Made With Love
          v-icon(color='pink' small='') favorite
      // Go to top
      app-fab
      // theme setting
      //- v-btn.setting-fab(small='' fab='' dark='' falt='' fixed='' top='top' right='right' color='red' @click='openThemeSettings')
      //-   v-icon settings
      v-navigation-drawer.setting-drawer(temporary='' right='' v-model='rightDrawer' hide-overlay='' fixed='')
        theme-settings
  template(v-else='')
    transition
      keep-alive
        router-view
  v-snackbar(:timeout='3000' bottom='' right='' :color='snackbar.color' v-model='snackbar.show')
    | {{ snackbar.text }}
    v-btn(dark='' flat='' @click.native='snackbar.show = false' icon='')
      v-icon close
</template>
<script>
import AppDrawer from '@/components/AppDrawer';
import AppToolbar from '@/components/AppToolbar';
import AppFab from '@/components/AppFab';
import PageHeader from '@/components/PageHeader';
import menu from '@/api/menu';
import ThemeSettings from '@/components/ThemeSettings';
import AppEvents from  './event';
import colors from 'vuetify/es5/util/colors'
import Cookies from 'js-cookie'
import { getToken } from '@/util/auth'

export default {
  components: {
    AppDrawer,
    AppToolbar,
    AppFab,
    PageHeader,
    ThemeSettings
  },
  data: () => ({
    expanded: true,
    rightDrawer: false,
    snackbar: {
      show: false,
      text: '',
      color: '',
    }
  }),

  computed: {
  },

  created () {
    AppEvents.forEach(item => {
      this.$on(item.name, item.callback);
    });
    window.getApp = this;
  },
  methods: {
    openThemeSettings () {
      this.$vuetify.goTo(0);
      this.rightDrawer = (!this.rightDrawer);
    }
  },
  mounted() {
    const val = Cookies.get('theme-primary')
    const base = colors[val] ? colors[val].base : colors["blue"]
    this.$vuetify.theme.primary = base
    this.$vuetify.dark = (Cookies.get('theme-side') === 'dark')
    if (this.$router.currentRoute.name !== 'Login' && !getToken()) {
      window.getApp.$emit('APP_LOGOUT')
    }
  }
};
</script>


<style lang="stylus" scoped>
  .setting-fab
    top:50%!important;
    right:0;
    border-radius:0
  .page-wrapper
    min-height:calc(100vh - 64px - 50px - 81px );
    height: 100%;

</style>
