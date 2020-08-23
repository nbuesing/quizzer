<template>
    <div style="padding: 10px">
        <form novalidate class="md-layout" @submit.prevent="start">
            <md-card class="md-layout-item">
                <md-card-header>
                    <div class="md-title">Number of Questions</div>
                </md-card-header>
                <md-card-content>
                    <div>
                        <md-field>
                            <md-select v-model="count" name="count" id="count">
                                <md-option value="2">2</md-option>
                                <md-option value="3">3</md-option>
                                <md-option value="4">4</md-option>
                                <md-option value="5">5</md-option>
                                <md-option value="6">6</md-option>
                                <md-option value="7">7</md-option>
                                <md-option value="8">8</md-option>
                                <md-option value="9">9</md-option>
                                <md-option value="10">10</md-option>
                            </md-select>
                        </md-field>
                    </div>
                </md-card-content>
                <md-card-actions md-alignment="left">
                    <md-button type="submit" class="md-primary" :disabled="sending">Start</md-button>
                </md-card-actions>
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
    //
    // let api = axios.create({
    //     baseURL: 'http://localhost:9080',
    //     // baseURL: 'http://192.168.0.175:9080',
    //     headers: {
    //         'Content-Type': 'application/json'
    //     }
    // });

    export default {
        name: 'start',
        methods: {
            start() {
                this.sending = true;
                api.post("quizzes/start", {
                        "quiz_id": this.generateQuizId(),
                        "user_id": this.$store.state.userId,
                        "count": Number(this.count)
                    }
                ).then(response => {

                    console.log("Headers : " + response.headers);

                    this.$router.replace({
                        name: "quiz",
                        params: {
                            foo: response.data
                        }
                    });
                }).finally(() => {
                    this.sending = false
                })
            },
            generateQuizId() {
              let text = ""
              let chars = "abcdefghijklmnopqrstuvwxyz"
              for (let i = 0; i < 10; i++) {
                text += chars.charAt(Math.floor(Math.random() * chars.length))
              }
              return "qz_" + text
          },

        },
        data() {
            return {
                sending: false,
                count: 5
            };
        },
        mounted() {
        },
        beforeDestroy() {
        }
    };
</script>