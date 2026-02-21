import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      component: () => import('@/views/auth/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      component: () => import('@/views/auth/Register.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/home',
      component: () => import('@/views/Home.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/manager',
      component: () => import('@/views/Manager.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          component: () => import('@/views/manager/Dashboard.vue'),
        },
        {
          path: 'elders',
          component: () => import('@/views/manager/ElderList.vue'),
        },
        {
          path: 'elders/create',
          component: () => import('@/views/manager/ElderForm.vue'),
        },
        {
          path: 'elders/:id/edit',
          component: () => import('@/views/manager/ElderForm.vue'),
        },
        {
          path: 'elders/:id',
          component: () => import('@/views/manager/ElderDetail.vue'),
        },
        {
          path: 'volunteers',
          component: () => import('@/views/manager/VolunteerList.vue'),
        },
        {
          path: 'volunteers/create',
          component: () => import('@/views/manager/VolunteerForm.vue'),
        },
        {
          path: 'volunteers/:id/edit',
          component: () => import('@/views/manager/VolunteerForm.vue'),
        },
        {
          path: 'volunteers/:id',
          component: () => import('@/views/manager/VolunteerDetail.vue'),
        },
        {
          path: 'tasks',
          component: () => import('@/views/manager/TaskList.vue'),
        },
        {
          path: 'tasks/:id',
          component: () => import('@/views/manager/TaskDetail.vue'),
        },
        {
          path: 'emergency',
          component: () => import('@/views/manager/EmergencyList.vue'),
        },
        {
          path: 'emergency/:id',
          component: () => import('@/views/manager/EmergencyDetail.vue'),
        },
        {
          path: 'products',
          component: () => import('@/views/manager/ProductList.vue'),
        },
        {
          path: 'products/create',
          component: () => import('@/views/manager/ProductForm.vue'),
        },
        {
          path: 'products/:id/edit',
          component: () => import('@/views/manager/ProductForm.vue'),
        },
        {
          path: 'exchanges',
          component: () => import('@/views/manager/ExchangeList.vue'),
        },
        {
          path: 'exchanges/create',
          component: () => import('@/views/manager/ExchangeForm.vue'),
        },
        {
          path: 'exchanges/:id',
          component: () => import('@/views/manager/ExchangeDetail.vue'),
        },
        {
          path: 'appeals',
          component: () => import('@/views/manager/AppealList.vue'),
        },
        {
          path: 'appeals/:id',
          component: () => import('@/views/manager/AppealDetail.vue'),
        },
        {
          path: 'appeals/:id/process',
          component: () => import('@/views/manager/AppealProcess.vue'),
        },
        {
          path: 'notifications',
          component: () => import('@/views/manager/NotificationForm.vue'),
        },
        {
          path: 'points',
          component: () => import('@/views/manager/PointsManager.vue'),
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const token = localStorage.getItem('token')

  if (requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router