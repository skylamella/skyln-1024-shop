<template>
    <el-row :gutter="20">
        <el-col :span="4">
            <div class="grid-content" />
        </el-col>
        <el-col :span="16">
            <div class="grid-content">
                123
            </div>
        </el-col>
        <el-col :span="4">
            <div class="grid-content" />
        </el-col>
    </el-row>
</template>

<script setup>
function loginCheck() {
    return false;
}
</script>

<style>

</style>
