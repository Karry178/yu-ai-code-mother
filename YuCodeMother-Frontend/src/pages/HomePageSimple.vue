<template>
  <div id="homePage" style="padding: 40px; background: linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%); min-height: 100vh;">
    <div style="max-width: 1200px; margin: 0 auto;">
      <!-- 标题 -->
      <div style="text-align: center; margin-bottom: 40px;">
        <h1 style="font-size: 48px; color: #1e293b; margin-bottom: 16px;">AI 应用生成平台</h1>
        <p style="font-size: 20px; color: #64748b;">一句话轻松创建网站应用</p>
      </div>

      <!-- 输入框 -->
      <div style="max-width: 800px; margin: 0 auto 40px;">
        <a-textarea
          v-model:value="userPrompt"
          placeholder="帮我创建个人博客网站"
          :rows="4"
          style="border-radius: 16px; font-size: 16px;"
        />
        <div style="margin-top: 12px; text-align: right;">
          <a-button type="primary" size="large" @click="handleCreate" :loading="creating">
            创建应用
          </a-button>
        </div>
      </div>

      <!-- 快捷按钮 -->
      <div style="display: flex; gap: 12px; justify-content: center; margin-bottom: 60px; flex-wrap: wrap;">
        <a-button @click="setPrompt('创建一个现代化的个人博客网站')">个人博客</a-button>
        <a-button @click="setPrompt('设计一个专业的企业官网')">企业官网</a-button>
        <a-button @click="setPrompt('构建一个功能完整的在线商城')">在线商城</a-button>
        <a-button @click="setPrompt('制作一个精美的作品展示网站')">作品展示</a-button>
      </div>

      <!-- 我的作品 -->
      <div style="margin-bottom: 60px;">
        <h2 style="font-size: 32px; margin-bottom: 24px; color: #1e293b;">我的作品</h2>
        <div v-if="myApps.length > 0" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 24px;">
          <div v-for="app in myApps" :key="app.id" style="background: white; border-radius: 16px; padding: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
            <h3 style="margin: 0 0 8px; font-size: 18px;">{{ app.appName || '未命名应用' }}</h3>
            <p style="margin: 0; color: #666;">{{ app.user?.userName || '未知用户' }}</p>
          </div>
        </div>
        <div v-else style="text-align: center; padding: 40px; color: #999;">
          <p>暂无应用，快去创建一个吧！</p>
        </div>
      </div>

      <!-- 精选案例 -->
      <div>
        <h2 style="font-size: 32px; margin-bottom: 24px; color: #1e293b;">精选案例</h2>
        <div v-if="featuredApps.length > 0" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 24px;">
          <div v-for="app in featuredApps" :key="app.id" style="background: white; border-radius: 16px; padding: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
            <h3 style="margin: 0 0 8px; font-size: 18px;">{{ app.appName || '未命名应用' }}</h3>
            <p style="margin: 0; color: #666;">{{ app.user?.userName || '官方' }}</p>
          </div>
        </div>
        <div v-else style="text-align: center; padding: 40px; color: #999;">
          <p>暂无精选案例</p>
        </div>
      </div>

      <!-- 调试信息 -->
      <div style="margin-top: 40px; padding: 20px; background: #f0f0f0; border-radius: 8px;">
        <h3>调试信息：</h3>
        <p>用户提示词: {{ userPrompt || '(空)' }}</p>
        <p>我的应用数量: {{ myApps.length }}</p>
        <p>精选应用数量: {{ featuredApps.length }}</p>
        <p>创建中: {{ creating ? '是' : '否' }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'

const userPrompt = ref('')
const creating = ref(false)
const myApps = ref<any[]>([])
const featuredApps = ref<any[]>([])

const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
  message.success('提示词已设置')
}

const handleCreate = () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }
  creating.value = true
  setTimeout(() => {
    creating.value = false
    message.success('创建成功（模拟）')
  }, 2000)
}

onMounted(() => {
  console.log('HomePageSimple 组件已挂载')
  message.info('简化版首页已加载')
  
  // 模拟数据
  myApps.value = [
    { id: 1, appName: '测试应用1', user: { userName: '测试用户' } },
    { id: 2, appName: '测试应用2', user: { userName: '测试用户' } },
  ]
  
  featuredApps.value = [
    { id: 3, appName: '精选应用1', user: { userName: '官方' } },
    { id: 4, appName: '精选应用2', user: { userName: '官方' } },
  ]
})
</script>
