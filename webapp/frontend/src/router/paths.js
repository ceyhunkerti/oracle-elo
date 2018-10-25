export default [

  {
    path: '*',
    meta: {
      public: true
    },
    redirect: {
      path: '/404'
    }
  },
  {
    path: '/404',
    meta: {
      public: true
    },
    name: 'NotFound',
    component: () => import(
      /* webpackChunkName: "routes" */
      /* webpackMode: "lazy-once" */
      `@/pages/NotFound.vue`
    )
  },
  {
    path: '/403',
    meta: {
      public: true
    },
    name: 'AccessDenied',
    component: () => import(
      /* webpackChunkName: "routes" */
      /* webpackMode: "lazy-once" */
      `@/pages/Deny.vue`
    )
  },
  {
    path: '/500',
    meta: {
      public: true
    },
    name: 'ServerError',
    component: () => import(
      /* webpackChunkName: "routes" */
      /* webpackMode: "lazy-once" */
      `@/pages/Error.vue`
    )
  },
  {
    path: '/login',
    meta: {
      public: true
    },
    name: 'Login',
    component: () => import(
      /* webpackChunkName: "routes" */
      /* webpackMode: "lazy-once" */
      `@/pages/Login.vue`
    )
  },
  {
    path: '/',
    meta: { },
    name: 'Root',
    redirect: {
      name: 'Dashboard'
    }
  },
  {
    path: '/dashboard',
    meta: { breadcrumb: false },
    name: 'Dashboard',
    component: () => import(
      /* webpackChunkName: "routes" */
      /* webpackMode: "lazy-once" */
      `@/pages/Dashboard.vue`
    )
  },
  {
    path: '/extractions',
    meta: { },
    name: 'Extractions',
    props: (route) => ({ type: route.query.type }),
    component: () => import(
      `@/pages/Extractions.vue`
    )
  },
  {
    path: '/logs',
    meta: { },
    name: 'Logs',
    props: (route) => ({ type: route.query.type }),
    component: () => import(
      `@/pages/PlLogs.vue`
    )
  }
]
