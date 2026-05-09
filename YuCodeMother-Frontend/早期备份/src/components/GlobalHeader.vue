<template>
  <a-layout-header class="header">
    <div class="header-content">
      <!-- Logo 和标题 -->
      <div class="logo-section">
        <img src="@/assets/AI代码网站logo.png" alt="Logo" class="logo" />
        <span class="title">Yu AI Code Mother</span>
      </div>

      <!-- 导航菜单 -->
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="horizontal"
        :style="{ flex: 1, minWidth: 0, border: 'none' }"
        @click="handleMenuClick"
      >
        <a-menu-item v-for="item in menuItems" :key="item.key">
          {{ item.label }}
        </a-menu-item>
      </a-menu>

      <!-- 右侧用户区域 -->
      <div class="user-section">
        <div v-if="loginUserStore.loginUser.id">
          <a-dropdown>
            <a-space style="cursor: pointer">
              <a-avatar :src="loginUserStore.loginUser.userAvatar" />
              {{ loginUserStore.loginUser.userName ?? '无名氏' }}
            </a-space>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="doLogout">
                  <LogoutOutlined />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
        <div v-else>
          <a-button type="primary" href="/user/login">登录</a-button>
        </div>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { HomeOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import { userLogout } from '@/api/userController.ts'
import { type MenuProps, message } from 'ant-design-vue'

// 获取登录用户状态 - 引用loginUserStore
const loginUserStore = useLoginUserStore()

const router = useRouter()

// 当前选中的菜单项
const selectedKeys = ref<string[]>(['home'])

// 菜单配置项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

// 菜单点击事件
const handleMenuClick = ({ key }: { key: string }) => {
  // 直接使用 key 作为路由路径
  router.push(key)
}

// 退出登录
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败,' + res.data.message)
  }
}
</script>

<style scoped>
.header {
  background: #fff;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 999;
}

.header-content {
  display: flex;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
}

.logo-section {
  display: flex;
  align-items: center;
  margin-right: 40px;
  white-space: nowrap;
}

.logo {
  height: 40px;
  width: auto;
  margin-right: 12px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.user-section {
  margin-left: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .title {
    display: none;
  }

  .logo {
    height: 32px;
  }

  .header-content {
    padding: 0 12px;
  }
}
</style>
