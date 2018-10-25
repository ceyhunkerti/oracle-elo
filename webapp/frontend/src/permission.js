
// import router from './router'
// import store from './store'
// import NProgress from 'nprogress' // progress bar
// import 'nprogress/nprogress.css'// progress bar style
// import { getToken } from '@/util/auth' // getToken from cookie

// if (!getToken()) {
//   window.location = '/login'
// }

// NProgress.configure({ showSpinner: false })// NProgress Configuration

// // permission judge function
// function hasPermission(role, permissionRoles) {
//   if (role === 'MASTER') return true // admin permission passed directly
//   if (!permissionRoles) return true
//   return permissionRoles.indexOf(role) >= 0
// }

// const whiteList = ['/login', '/authredirect']// no redirect whitelist

// router.beforeEach((to, from, next) => {
//   if (!getToken()) {
//     next('/login')
//   }
// })

// router.beforeEach((to, from, next) => {
//   NProgress.start() // start progress bar
//   if (getToken()) { // determine if there has token
//     /* has token*/
//     if (to.path === '/login') {
//       next({ path: '/dashboard' })
//       NProgress.done() // if current page is dashboard will not trigger	afterEach hook, so manually handle it
//     } else {
//       if (!store.getters.role) { // 判断当前用户是否已拉取完user_info信息
//         store.dispatch('GetUserInfo').then(res => { // 拉取user_info
//         }).catch((err) => {
//           store.dispatch('FedLogOut').then(() => {
//             console.log(err)
//             next({ path: '/' })
//           })
//         })
//       } else {
//         if (hasPermission(store.getters.role, to.meta.roles)) {
//           next()//
//         } else {
//           next({ path: '/401', replace: true, query: { noGoBack: true }})
//         }
//       }
//     }
//   } else {
//     /* has no token*/
//     if (whiteList.indexOf(to.path) !== -1) {
//       next()
//     } else {
//       next('/login')
//       NProgress.done() // if current page is login will not trigger afterEach hook, so manually handle it
//     }
//   }
// })

// router.afterEach(() => {
//   NProgress.done() // finish progress bar
// })
