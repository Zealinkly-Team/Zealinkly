<template>
  <div id="app">
    <n-message-provider>
      <router-view />
    </n-message-provider>
  </div>
</template>

<script setup>
import { NMessageProvider } from 'naive-ui'
// App.vue 根组件
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  background-color: #f5f7fa;
}

#app {
  min-height: 100vh;
}

.el-message {
  min-width: 300px;
}

.el-table {
  margin-top: 20px;
}

.el-form {
  max-width: 600px;
}

.el-dialog {
  width: 90%;
  max-width: 800px;
}
</style>