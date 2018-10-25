<template lang="pug">
v-toolbar(color='primary' fixed app)
  v-toolbar-title.ml-0.pl-3
    v-toolbar-side-icon(@click.stop='handleDrawerToggle' dark)
  v-spacer
  v-btn(icon dark href='https://github.com/bluecolor/elo')
    v-icon fa fa-github
  v-btn(icon @click="onThemeSettings" dark)
    v-icon settings
  v-btn(icon @click='handleFullScreen()' dark)
    v-icon fullscreen
  v-menu.elelvation-1(style="display:none" offset-y='' origin='center center' :nudge-bottom='14' transition='scale-transition')
    v-btn(icon flat dark slot='activator')
      v-badge(color='red' overlap='')
        span(slot='badge') 3
        v-icon(medium) notifications
    notification-list
  v-menu(offset-y='' origin='center center' :nudge-bottom='10' transition='scale-transition')
    v-btn(icon large='' flat='' slot='activator')
      v-avatar(size='32px')
        img(src='/static/avatar/cap.png' alt='')
    v-list.pa-0
      v-list-tile(
        v-for='(item,index) in items' :to='!item.href ? { name: item.name } : null'
        :href='item.href' @click='item.click' ripple='ripple'
        :disabled='item.disabled' :target='item.target' rel='noopener' :key='index'
      )
        v-list-tile-action(v-if='item.icon')
          v-icon {{ item.icon }}
        v-list-tile-content
          v-list-tile-title {{ item.title }}

</template>
<script>

import NotificationList from '@/components/widgets/list/NotificationList'
import Util from '@/util';
import colors from 'vuetify/es5/util/colors';
import Cookies from 'js-cookie'

export default {
  name: 'app-toolbar',
  components: {
    NotificationList
  },
  data: () => ({
    items: [
      {
        icon: 'account_circle',
        href: '#',
        title: 'Profile',
        click: (e) => {
          window.getApp.$toast('Coming soon!')
        }
      },
      {
        icon: 'settings',
        href: '#',
        title: 'Settings',
        click: (e) => {
          window.getApp.$toast('Coming soon!')
        }
      },
      {
        icon: 'fullscreen_exit',
        href: '#',
        title: 'Logout',
        click: (e) => {
          window.getApp.$emit('APP_LOGOUT');
        }
      }
    ],
  }),
  computed: {
    toolbarColor () {
      return this.$vuetify.options.extra.mainNav;
    }
  },
  methods: {
    onThemeSettings() {
      this.$emit('themeSettings')
    },
    handleDrawerToggle () {
      window.getApp.$emit('APP_DRAWER_TOGGLED');
    },
    handleFullScreen () {
      Util.toggleFullScreen();
    }
  },
  mounted() {
  }
};
</script>
