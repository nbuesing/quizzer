<template>
    <div class="page-container">
        <md-app md-waterfall>
            <md-app-toolbar class="md-primary">
                <md-button class="md-icon-button" @click="menuVisible = !menuVisible">
                    <md-icon>menu</md-icon>
                </md-button>
                <span class="md-title">Quizzer :: name="{{ this.$store.state.name }}"</span>
            </md-app-toolbar>
            <md-app-drawer :md-active.sync="menuVisible">
                <md-button @click="navigate('start')">
                    <md-icon>launch</md-icon>
                    Start Quiz
                </md-button>
                <br/>
<!--                <md-button @click="navigate('createUser')">-->
<!--                    <md-icon>perm_identity</md-icon>-->
<!--                    Update User-->
<!--                </md-button>-->

              <!--                <br/>-->
<!--                <md-button @click="navigate('leaderboard')">-->
<!--                    <md-icon>list</md-icon>-->
<!--                    Leaderboard-->
<!--                </md-button>-->

            </md-app-drawer>
            <md-app-content>
                <router-view></router-view>
            </md-app-content>
        </md-app>
    </div>
</template>
<style>
    .fade-enter-active,
    .fade-leave-active {
        transition: opacity 0.1s;
    }

    .fade-enter,
    .fade-leave-to
        /* .fade-leave-active in <2.1.8 */
    {
        opacity: 0;
    }

    .md-app {
        /*max-height: 600px;*/
        border: 1px solid rgba(#000, .12);
    }

    .md-app-drawer {
        width: 130px;
        /*max-width: calc(100vw - 125px);*/
    }

    .md-app-toolbar {
        /*height: 196px;*/
    }
</style>
<script>

    import axios from 'axios'
    import {generate} from 'project-name-generator'

    let apiUrl = window.location.protocol + "//" + window.location.hostname + ":9080";

    let api = axios.create({
        baseURL: apiUrl,
        headers: {
            'Content-Type': 'application/json'
        }
    });


    export default {
        data() {
            return {
                menuVisible: false,
            }
        },
        methods: {
            navigate(location) {
                this.menuVisible = false;
                this.$router.replace({name: location});
            },
            randomString(length) {
                let text = ""
                let chars = "1234567890"
                for (let i = 0; i < length; i++) {
                    text += chars.charAt(Math.floor(Math.random() * chars.length))
                }
                return text
            },
            capitalize(string) {
                return string.charAt(0).toUpperCase() + string.slice(1);
            },
            getRandomInt(min, max) {
                return Math.floor(Math.random() * (max - min)) + min;
            },
            generateName(){
                var name1 = [
                    "affable",
                    "agreeable",
                    "ambitious",
                    "amusing",
                    "brave",
                    "bright",
                    "calm",
                    "careful",
                    "charming",
                    "courteous",
                    "creative",
                    "decisive",
                    "determined",
                    "diligent",
                    "discreet",
                    "dynamic",
                    "emotional",
                    "energetic",
                    "exuberant",
                    "faithful",
                    "fearless",
                    "forceful",
                    "friendly",
                    "funny",
                    "generous",
                    "gentle",
                    "good",
                    "helpful",
                    "honest",
                    "humorous",
                    "impartial",
                    "intuitive",
                    "inventive",
                    "loyal",
                    "modest",
                    "neat",
                    "nice",
                    "optimistic",
                    "patient",
                    "persistent",
                    "placid",
                    "plucky",
                    "polite",
                    "powerful",
                    "practical",
                    "quiet",
                    "rational",
                    "reliable",
                    "reserved",
                    "sensible",
                    "sensitive",
                    "shy",
                    "sincere",
                    "sociable",
                    "tidy",
                    "tough",
                    "versatile",
                    "willing",
                    "witty"
                ];
                var name2 = [
                    "Broker",
                    "Cleaner",
                    "Cluster",
                    "CommitLog",
                    "Connect",
                    "Consumer",
                    "Election",
                    "Jitter",
                    "KIP",
                    "Kafka",
                    "Leader",
                    "Listener",
                    "Message",
                    "Offset",
                    "Partition",
                    "Processor",
                    "Producer",
                    "Protocol",
                    "Purgatory",
                    "Quota",
                    "Replica",
                    "Retention",
                    "RocksDB",
                    "Segment",
                    "Schema",
                    "Streams",
                    "Subject",
                    "Timeout",
                    "Tombstone",
                    "Topic",
                    "Topology",
                    "Transaction",
                    "Zombie",
                    "Zookeeper"
                ];

                const name = this.capitalize(name1[this.getRandomInt(0, name1.length)]) + ' ' + this.capitalize(name2[this.getRandomInt(0, name2.length + 1)]);

                window.console.log("Name : " + name);

                return name;
            }
        },
        created() {

            if (this.$store.state.name == null) {
                this.$store.state.name = this.generateName();
                //this.$store.state.name = generate(null).dashed;
            }

            if (this.$store.state.userId == null) {
                this.$store.state.userId = this.randomString(12);
                api.post("users", {
                        "user_id": this.$store.state.userId,
                        "name": this.$store.state.name
                    }
                ).then(response => {
                    window.console.log("updating user_id=" + this.$store.state.userId + ", name=" + this.$store.state.name);
                }).finally(() => {
                    window.console.log("user endpoint called.");
                })
            }
        }
    };
</script>
