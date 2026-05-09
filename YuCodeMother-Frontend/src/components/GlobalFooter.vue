<template>
  <div class="global-footer">
    <div class="footer-content">
      <a href="https://github.com/Karry178" target="_blank" rel="noopener noreferrer">
        我的Github by 程序员Karry178
      </a>
      <span class="divider">|</span>
      <a href="https://gitee.com/Karry178" target="_blank" rel="noopener noreferrer">
        我的Gitee by 程序员Karry178
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
// 无需额外的响应式数据
</script>

<style scoped>
.global-footer {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  text-align: center;
  padding: 20px;
  margin-top: 40px;
  border-top: 1px solid rgba(102, 126, 234, 0.1);
}

.footer-content {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.footer-content a {
  color: #1890ff;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-content a:hover {
  color: #40a9ff;
}

.divider {
  margin: 0 12px;
  color: #d9d9d9;
}
</style>
