import { createApp } from 'vue'
import App from './App.vue'
import './style.css'
import router from './router/index'
import store from './stores/index'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import * as Icons from '@element-plus/icons-vue'

const app = createApp(App)

// 加载所有element的icon图标
for (const iconName in Icons) {
    if (Reflect.has(Icons, iconName)) {
        const item = Icons[iconName]
        app.component(iconName, item)
    }
}

app.use(router)
    .use(store)
    .use(ElementPlus, {
        locale: zhCn,
    })
    .mount('#app') 
