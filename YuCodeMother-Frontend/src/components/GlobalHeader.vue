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
        <a-button type="primary">登录</a-button>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 当前选中的菜单项
const selectedKeys = ref<string[]>(['home'])

// 菜单配置
const menuItems = ref([
  { key: 'home', label: '首页', path: '/' },
  { key: 'about', label: '关于', path: '/about' }
])

// 菜单点击事件
const handleMenuClick = ({ key }: { key: string }) => {
  const item = menuItems.value.find((menu) => menu.key === key)
  if (item?.path) {
    router.push(item.path)
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
