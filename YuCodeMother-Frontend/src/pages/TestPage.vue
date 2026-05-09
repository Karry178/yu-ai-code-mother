<template>
  <div style="padding: 20px; background: white;">
    <h1>测试页面</h1>
    <p>如果你能看到这个页面，说明Vue路由和基本渲染是正常的。</p>
    <a-button type="primary" @click="testClick">点击测试</a-button>
    <p v-if="clicked">按钮点击成功！</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const clicked = ref(false)

const testClick = () => {
  clicked.value = true
  console.log('按钮被点击了')
}
</script>
