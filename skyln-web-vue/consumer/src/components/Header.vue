<template>
    <el-row class="header-el-row" :gutter="20">
        <el-col :span="4">
            <div class="grid-content" />
        </el-col>
        <el-col :span="16" style="line-height: 1.9em;">
            <div class="left-div" v-if="loginCheck()">
                <el-button type="warning" link><span class="my-button-class">登录</span></el-button>
                <el-button type="info" link><span class="my-button-class">注册</span></el-button>
            </div>
            <div class="left-div" v-if="!loginCheck()">
                <el-dropdown style="line-height: 2.6em;margin-left: 1em;color: #909399;" placement="bottom-start">
                    <span class="el-dropdown-link my-button-class">
                        <el-icon>
                            <UserFilled />
                        </el-icon>
                        {{name}}
                        <el-icon class="el-icon--right">
                            <arrow-down />
                        </el-icon>
                    </span>
                    <template #dropdown>
                        <el-card class="box-card">
                            <template #header>
                                <div class="card-header">
                                    <el-button link>账号管理</el-button>
                                    <el-button type="danger" link>退出</el-button>
                                </div>
                            </template>
                            <div class="left-div">
                                <el-avatar :src="head_img" />
                            </div>
                            <div class="right-div">

                            </div>
                        </el-card>
                    </template>
                </el-dropdown>
            </div>
            <div class="right-div">
                <el-button type="info" link><span class="my-button-class">我的</span></el-button>
                <el-button type="info" link>
                    <span class="my-button-class">
                        <span style="color:#e6a23c;">
                            <el-icon>
                                <ShoppingCartFull />
                            </el-icon>
                        </span>
                        购物车
                    </span>
                </el-button>
                <el-dropdown style="line-height: 2.6em;margin-left: 1em;color: #909399;">
                    <span class="el-dropdown-link my-button-class">
                        <span style="color:#e6a23c;">
                            <el-icon>
                                <StarFilled />
                            </el-icon>
                        </span>
                        收藏夹
                        <el-icon class="el-icon--right">
                            <arrow-down />
                        </el-icon>
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item>
                                <el-button type="info" link>收藏的商品</el-button>
                            </el-dropdown-item>
                            <el-dropdown-item>
                                <el-button type="info" link>收藏的商铺</el-button>
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </div>
        </el-col>
        <el-col :span="4">
            <div class="grid-content" />
        </el-col>
    </el-row>
</template>

<script setup>
const loginCheck = function () {
    const token = localStorage.getItem("token");
    const username = localStorage.getItem("username");
    const name = localStorage.getItem("name");
    const head_img = localStorage.getItem("head_img");
    if (token !== null && token.length > 0 &&
        username !== null && username.length > 0 &&
        name !== null && name.length > 0 &&
        head_img !== null && head_img.length > 0
    ) {
        return false;
    } else {
        return true;
    }
}
</script>

<style>
.header-el-row {
    background-color: #f5f5f5;
}

.my-button-class:hover {
    color: #e6a23c;
}

.left-div {
    float: left;
}

.right-div {
    float: right;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.box-card {
    width: 300px;
}
</style>
