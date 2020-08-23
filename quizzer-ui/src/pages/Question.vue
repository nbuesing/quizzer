<template>
    <div style="padding: 10px">
        FOO
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
    //     baseURL: 'http://192.168.0.175:9080',
    //     headers: {
    //         'Content-Type': 'application/json'
    //     }
    // });

    export default {
        name: 'questions',
        methods: {
        },
        data() {
            return {
            };
        },
        mounted() {
        },
        beforeDestroy() {
        },
    };
</script>