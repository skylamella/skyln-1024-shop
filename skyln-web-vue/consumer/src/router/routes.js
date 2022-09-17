import Home from '../views/Home.vue'
import Course from '../views/Course.vue'

const routes = [
    {
        path: '/',
        component: Home
    },
    {
        path: '/home',
        component: Home
    },
    {
        path: '/course',
        component: Course
    },
]

export default routes