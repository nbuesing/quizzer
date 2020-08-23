

import DashboardLayout from '../pages/Layout/DashboardLayout.vue'
import CreateUser from '../pages/CreateUser.vue'
import Quiz from '../pages/Quiz.vue'
import QuizStart from '../pages/QuizStart.vue'
import QuizResult from '../pages/QuizResult.vue'
import Qrcode from '../pages/Qrcode.vue'
import QuizLeaderboard from "../pages/QuizLeaderboard";

const routes = [
    {
        path: '/',
        component: DashboardLayout,
        redirect: '/start',
        children: [
            {
                name: 'createUser',
                path: 'create-user',
                props: true,
                component: CreateUser
            },
            {
                name: 'start',
                path: 'start',
                props: true,
                component: QuizStart
            },
            {
                name: 'quiz',
                path: 'quiz',
                props: true,
                component: Quiz
            },
            {
                name: 'result',
                path: 'result',
                props: true,
                component: QuizResult
            },
            {
                name: 'leaderboard',
                path: 'leaderboard',
                props: true,
                component: QuizLeaderboard
            }
        ]
    },
    {
        path: '/qrcode',
        component: Qrcode
    }
]

export default routes
