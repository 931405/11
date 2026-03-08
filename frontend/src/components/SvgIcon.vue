<template>
  <el-icon :class="customClass" :style="{ color: color, fontSize: size + 'px' }">
    <component :is="name" />
  </el-icon>
</template>

<script setup>
import { defineProps } from 'vue'

defineProps({
  name: {
    type: String,
    required: true
  },
  color: {
    type: String,
    default: 'inherit'
  },
  size: {
    type: [Number, String],
    default: 16
  },
  customClass: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.el-icon {
  vertical-align: middle;
}
</style>
