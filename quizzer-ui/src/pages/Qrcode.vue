<template>
    <div style="padding: 50px">

        <md-card class="md-layout-item">
            <md-card-header>
                <div class="md-title">{{ value }}</div>
            </md-card-header>
            <md-card-content>
                <qrcode-vue :value="value" :size="size" level="H"></qrcode-vue>
            </md-card-content>
        </md-card>
    </div>
</template>
<script>

    import api from '../plugins/api.js'

    import QrcodeVue from 'qrcode.vue'

    export default {

        data() {
            return {
                value: 'http://192.168.64.233:8080',
                size: 300
            }
        },
        components: {
            QrcodeVue,
        },
    }
</script> 