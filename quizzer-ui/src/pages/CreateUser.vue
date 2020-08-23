<template>
    <div style="padding: 10px">
        <form novalidate class="md-layout" @submit.prevent="start">
            <md-card class="md-layout-item">
                <md-card-header>
                    <div class="md-title">Create User</div>
                </md-card-header>
                <md-card-content>
                    <div>
                        <md-field>
                            <md-input v-model="name" name="username" id="username">
                            </md-input>
                        </md-field>
                    </div>
                </md-card-content>
                <md-card-actions md-alignment="left">
                    <md-button type="submit" class="md-primary" :disabled="sending">Create</md-button>
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
                api.post("users", {
                        "user_id": this.$store.state.userId,
                        "name": this.name
                    }
                ).then(response => {
                    this.$router.replace({
                        name: "start",
                        params: {
                            foo: response.data
                        }
                    });
                }).finally(() => {
                    this.sending = false
                    this.$store.state.name = this.name
                })
            }
        },
        data() {
            return {
                sending: false,
                name: null
            };
        },
        created() {
            this.name = this.$store.state.name
            window.console.log("setting name to " + this.name);
        }
    };
</script>