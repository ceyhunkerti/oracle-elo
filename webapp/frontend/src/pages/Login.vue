<template lang="pug">
v-app#login.primary
  v-content
    v-container(fluid='' fill-height='')
      v-layout(align-center='' justify-center='')
        v-flex(xs12='' sm8='' md4='' lg4='')
          v-card.elevation-1.pa-3
            v-card-text
              .layout.column.align-center
                img(src='/static/coffee.png' alt='Vue Material Admin' width='120' height='120')
                h1.flex.my-4.primary--text ELO
              v-form
                v-text-field(solo append-icon='person' name='login' label='Login' type='text' v-model='model.username')
                v-text-field#password(solo append-icon='lock' name='password' label='Password' type='password' v-model='model.password')
            v-card-actions
              v-btn(block='' color='primary' @click='login' :loading='loading') Login
  .footer
    p © {{year}} Blue
</template>

<script>
import colors from 'vuetify/es5/util/colors'
import moment from 'moment'
import Cookies from 'js-cookie'

export default {
  data: () => ({
    loading: false,
    model: {
      username: 'system',
      password: 'system'
    }
  }),
  computed: {
    year() { return moment().year() }
  },
  methods: {
    // login () {
    //   this.loading = true;
    //   setTimeout(() => {
    //     this.$router.push('/dashboard');
    //   }, 1000);
    // },
    login() {
      this.$store.dispatch('LoginByUsername', this.model).then(() => {
        this.$router.push({ path: '/dashboard' })
      }).catch(error => {
        console.log(error)
      })
    }
  },
  mounted() {
    const val = Cookies.get('theme-primary')
    const base = colors[val] ? colors[val].base : colors["blue"]
    this.$vuetify.theme.primary = base
  }
};
</script>
<style scoped lang="css">
  #login {
    height: 50%;
    width: 100%;
    position: absolute;
    top: 0;
    left: 0;
    content: "";
    z-index: 0;
  }
  .footer {
    position: fixed;
    left: 0;
    line-height: 30px;
    bottom: 0;
    width: 100%;
    background-color: #2196F3;
    color: white;
    text-align: center;
  }
  .footer p {
    margin-bottom: unset;
  }

</style>
