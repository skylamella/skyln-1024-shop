import { createRouter, createWebHashHistory } from 'vue-router'
import Home from '../components/Home.vue'
import Course from '../components/Course.vue'

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        {
            path: '/home',
            component: Home
        },
        {
            path: '/course',
            component: Course
        },
    ]
});

export default router;