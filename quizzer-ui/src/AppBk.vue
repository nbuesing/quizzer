<template>
    <div style="padding: 50px">
        <form novalidate class="md-layout" @submit.prevent="start">
            <md-card class="md-layout-item">
                <md-card-header>
                    <div class="md-title">Start a Quiz</div>
                </md-card-header>
                <md-card-content>
                </md-card-content>
                <md-card-actions md-alignment="left">
                    <div>
                        <md-field>
                            <md-select v-model="startQuiz.count" name="count" id="count" placeholder="2">
                                <md-option value="2">2</md-option>
                                <md-option value="5">5</md-option>
                            </md-select>
                        </md-field>
                    </div>
                    <md-button type="submit" class="md-primary" :disabled="sending">Start</md-button>
                </md-card-actions>
                <md-progress-bar md-mode="indeterminate" v-if="sending"/>
            </md-card>
        </form>
        <br/>
        <form novalidate class="md-layout" @submit.prevent="next" v-if="statement != null">
            <md-card class="md-layout-item">
                <md-card-header>
                    <div class="md-title">{{ statement }}</div>
                </md-card-header>
                <md-card-content>
                    <div class="md-layout">
                        <md-radio v-model="answer" value="a" @change="next">{{ a }}</md-radio>
                    </div>
                    <div class="md-layout">
                        <md-radio v-model="answer" value="b" @change="next">{{ b }}</md-radio>
                    </div>
                    <div class="md-layout">
                        <md-radio v-model="answer" value="c" @change="next">{{ c }}</md-radio>
                    </div>
                    <div class="md-layout">
                        <md-radio v-model="answer" value="d" @change="next">{{ d }}</md-radio>
                    </div>
                </md-card-content>
                <md-progress-bar md-mode="indeterminate" v-if="sending"/>
                <!--                <md-card-actions md-alignment="left">-->
                <!--                    <md-button type="submit" class="md-primary" :disabled="sending">Submit</md-button>-->
                <!--                </md-card-actions>-->
            </md-card>
        </form>
        <br/>
        <md-card class="md-layout-item">
            <md-card-header>
                <div class="md-title">Results</div>
            </md-card-header>
            <md-card-content>
                <div>
                    {{ this.result.correct }}
                </div>
                <div>
                    {{ this.result.total }}
                </div>
            </md-card-content>
        </md-card>
    </div>
</template>

<style lang="scss" scoped>
    .grid-container {
        display: grid;
        grid-template-columns: auto;
    }

    .md-radio {
        display: flex;
    }

</style>

<script>

    import axios from 'axios'

    let api = axios.create({
        baseURL: 'http://192.168.0.175:9080',
        headers: {
            'Content-Type': 'application/json'
        }
    });

    export default {
        name: 'quiz-taker',
        methods: {
            start() {
                this.sending = true;
                api.post("quizzes/start", {
                        "user_id": this.startQuiz.userId,
                        "count": this.startQuiz.count
                    }
                ).then(response => {
                    //window.console.log(response.data);
                    //this.userId = response.data.user_id;
                    this.quizId = response.data.quiz_id;
                    this.questionId = response.data.question_id;
                    this.statement = response.data.statement;
                    this.a = response.data.a;
                    this.b = response.data.b;
                    this.c = response.data.c;
                    this.d = response.data.d;
                    this.answer = null;
                }).finally(() => {
                    this.sending = false
                })
            },
            next() {
                this.sending = true;
                api.post("quizzes/answer", {
                        "user_id": this.userId,
                        "quiz_id": this.quizId,
                        "question_id": this.questionId,
                        "submitted_answer": this.answer
                    }
                ).then(response => {
                    window.console.log(response.data)

                    if (response.data.type === 'result') {

                        this.result.correct = response.data.correct;
                        this.result.total = response.data.questions;

                        this.quizId = response.data.quiz_id;
                        this.questionId = null;
                        this.statement = null;
                        this.a = null;
                        this.b = null;
                        this.c = null;
                        this.d = null;
                        this.answer = null;

                    } else {
                        this.quizId = response.data.quiz_id;
                        this.questionId = response.data.question_id;
                        this.statement = response.data.statement;
                        this.a = response.data.a;
                        this.b = response.data.b;
                        this.c = response.data.c;
                        this.d = response.data.d;
                        this.answer = null;
                    }
                }).finally(
                    this.sending = false
                )
            }
        },
        data() {
            return {
                sending: false,
                startQuiz: {
                    userId: "001",
                    count: 2
                },
                userId: "001",
                quizId: "",
                questionId: "",
                statement: null,
                a: "",
                b: "",
                c: "",
                d: "",
                answer: null,
                result: {
                    correct: null,
                    total: null
                }
            };
        },
        mounted() {
        },
        beforeDestroy() {
        },
    };
</script>