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
// 页脚组件
</script>

<style scoped>
.global-footer {
  text-align: center;
  padding: 16px 0;
  background-color: #f0f2f5;
  border-top: 1px solid #e8e8e8;
}

.footer-content {
  color: rgba(0, 0, 0, 0.65);
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
  margin: 0 16px;
  color: rgba(0, 0, 0, 0.45);
}
</style>
