<template>
    <div style="padding: 10px">
        <form novalidate class="md-layout" @submit.prevent="next" v-if="statement != null">
            <md-card class="md-layout-item">
                <md-card-header>
                    <div class="md-title">{{ statement }}</div>
                </md-card-header>
                <md-card-content>
                    <div class="md-layout" v-if="a !== ''">
                        <md-radio v-model="answer" value="a" @change="next">{{ a }}</md-radio>
                    </div>
                    <div class="md-layout" v-if="b !== ''">
                        <md-radio v-model="answer" value="b" @change="next">{{ b }}</md-radio>
                    </div>
                    <div class="md-layout" v-if="c !== ''">
                        <md-radio v-model="answer" value="c" @change="next">{{ c }}</md-radio>
                    </div>
                    <div class="md-layout" v-if="d !== ''">
                        <md-radio v-model="answer" value="d" @change="next">{{ d }}</md-radio>
                    </div>
                    <div class="md-layout">
                        <span>Difficulty : {{ difficulty }}</span>
                    </div>
                </md-card-content>
                <md-progress-bar md-mode="indeterminate" v-if="sending"/>
            </md-card>
        </form>
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

    import api from '../plugins/api.js'

    // import axios from 'axios'
    //
    // let api = axios.create({
    //     baseURL: 'http://localhost:9080',
    //     // baseURL: 'http://192.168.0.175:9080',
    //     headers: {
    //         'Content-Type': 'application/json'
    //     }
    // });

    export default {
        name: 'quiz-taker',
        props: ['foo'],
        methods: {
            next() {
                this.sending = true;
                api.post("quizzes/answer", {
                        "user_id": this.$store.state.userId,
                        "quiz_id": this.quizId,
                        "question_id": this.questionId,
                        "submitted_answer": this.answer
                    }
                )
                  .then(response => {

                    if (response.status === 202) {
                        setTimeout(() => {
                            console.log("REDIRECTING : " + response.data.location);
                            this.next2(response.data.location);
                        }, 500);

                        return;
                    }

                    window.console.log(response.data)
                    if (response.data.type === 'result') {
                        this.$router.replace({
                                name: "result",
                                params: {
                                    results: response.data
                                }
                            }
                        );
                    } else {
                        this.quizId = response.data.quiz_id;
                        this.questionId = response.data.question_id;
                        this.statement = response.data.statement;
                        this.a = response.data.a;
                        this.b = response.data.b;
                        this.c = response.data.c;
                        this.d = response.data.d;
                        this.difficulty = response.data.difficulty;
                        this.answer = null;
                    }
                }).finally(() => {
                        this.sending = false
                    }
                )
            },
          next2(url) {
                this.sending = true;
                api.get(url,
                )
                  .then(response => {

                    if (response.status === 202) {
                        setTimeout(() => {
                            console.log("REDIRECTING_AGAIN : " + response.data.location);
                            this.next2(response.data.location);
                        }, 500);

                        return;
                    }

                    window.console.log(response.data)
                    if (response.data.type === 'result') {
                      this.$router.replace({
                          name: "result",
                          params: {
                            results: response.data
                          }
                        }
                      );
                    } else {
                      this.quizId = response.data.quiz_id;
                      this.questionId = response.data.question_id;
                      this.statement = response.data.statement;
                      this.a = response.data.a;
                      this.b = response.data.b;
                      this.c = response.data.c;
                      this.d = response.data.d;
                      this.difficulty = response.data.difficulty;
                      this.answer = null;
                    }
                  }).finally(() => {
                    this.sending = false
                  }
                )
              }
        },
        data() {
            return {
                sending: false,
                userId: "001",
                quizId: "",
                questionId: "",
                statement: null,
                a: "",
                b: "",
                c: "",
                d: "",
                answer: null,
                difficulty: null
            };
        },
        activated() {
        },
        mounted() {
            //window.console.log(" >> " + JSON.stringify(this.foo));
            if (this.foo != null) {
                this.quizId = this.foo.quiz_id;
                this.questionId = this.foo.question_id;
                this.statement = this.foo.statement;
                this.a = this.foo.a;
                this.b = this.foo.b;
                this.c = this.foo.c;
                this.d = this.foo.d;
                this.difficulty = this.foo.difficulty;
                this.answer = null;
            }
        },
        beforeDestroy() {
        }
    };
</script>