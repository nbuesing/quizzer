<template>
    <div style="padding: 10px">
        <md-card class="md-layout-item">
            <md-card-header>
                <div class="md-title">Results</div>
            </md-card-header>
            <md-card-content>
                <md-list>
                    <md-list-item>Correct: {{ this.correct }}</md-list-item>
                    <md-list-item>Total: {{ this.total }}</md-list-item>
                    <md-list-item>Percentage: {{ ((this.correct / this.total) * 100.0).toFixed(2) }}%</md-list-item>
                </md-list>
            </md-card-content>
        </md-card>
      <md-button @click="navigate('start')">
        <md-icon>launch</md-icon>
        Start New Quiz
      </md-button>

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
        name: 'result',
        props: ['results'],
        methods: {
          navigate(location) {
            this.menuVisible = false;
            this.$router.replace({name: location});
          }
        },
        data() {
            return {
                userId: null,
                quizId: null,
                correct: null,
                total: null
            };
        },
        activated() {
        },
        mounted() {
            if (this.results != null) {
                this.userId = this.results.userId;
                this.quizId = this.results.quiz_id;
                this.correct = this.results.correct;
                this.total = this.results.questions;
            }
        },
        beforeDestroy() {
        }
    };
</script>