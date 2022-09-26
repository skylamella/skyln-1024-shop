<script setup>
// This starter template is using Vue 3 <script setup> SFCs
// Check out https://vuejs.org/api/sfc-script-setup.html#script-setup
import Header from './components/Header.vue';
import Bottom from './components/Bottom.vue';
</script>

<template>
  <div class="common-layout">
    <el-container>
      <el-header>
        <Header />
      </el-header>
      <el-main>
        <Bottom />
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
.el-header {
  --el-header-padding: 0 2px;
  --el-header-height: 2em;
  padding: var(--el-header-padding);
  box-sizing: border-box;
  flex-shrink: 0;
  height: var(--el-header-height);
  margin-top: -8px;
}
</style>
