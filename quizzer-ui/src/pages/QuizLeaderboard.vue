<template>
  <div style="padding: 5px">
    <md-card class="md-layout-item">
      <md-card-header>
        <div class="md-title">Leaderboard</div>
      </md-card-header>
      <md-card-content>
                <vue-good-table
                  :columns="columns"
                  :rows="quizResults"
                  theme="black-rhino"
                  :sort-options="{
            enabled: true,
            initialSortBy: {field: 'timestamp', type: 'desc'}
          }"
                  :pagination-options="{
            enabled: true,
            mode: 'records',
            perPage: 10,
            position: 'bottom',
            perPageDropdown: [10, 50, 100],
            dropdownAllowAll: false,
            setCurrentPage: 1,
            nextLabel: 'next',
            prevLabel: 'prev',
            rowsPerPageLabel: 'rows per page',
            ofLabel: 'of',
            pageLabel: 'page', // for 'pages' mode
            allLabel: 'all'
          }">
          <template slot="table-row" slot-scope="props">
            <span v-if="props.row.answered === props.row.questions">
              <span style="font-weight: bold; color: blue;">{{props.formattedRow[props.column.field]}}</span>
            </span>
            <span v-else>
              {{props.formattedRow[props.column.field]}}
            </span>
          </template>
          </vue-good-table>
<!--        <md-table id="eventTable" v-model="quizResults">-->
<!--          <md-table-row slot="md-table-row" slot-scope="{ item }">-->
<!--&lt;!&ndash;            <md-table-cell md-label="QuizId" md-sort-by="id" md-numeric>{{ item.quiz_id }}</md-table-cell>&ndash;&gt;-->
<!--            <md-table-cell md-label="Name" md-sort-by="name">{{ ((item.user_name) ? item.user_name : item.user_id) }}-->
<!--            </md-table-cell>-->
<!--            <md-table-cell md-label="Correct" md-sort-by="correct">{{ item.correct }}</md-table-cell>-->
<!--            <md-table-cell md-label="Total" md-sort-by="total">{{ item.questions }}</md-table-cell>-->
<!--            <md-table-cell md-label="Score" md-sort-by="percentage">{{ (item.correct / item.questions) | percentage  }}</md-table-cell>-->
<!--            <md-table-cell md-label="Date" md-sort-by="date">{{ item.date }}</md-table-cell>-->
<!--            <md-table-cell md-label="Time" md-sort-by="time">{{ item.time }}</md-table-cell>-->
<!--          </md-table-row>-->
<!--        </md-table>-->
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
<style lang="scss">

.vgt-table.black-rhino {
  font-size: 250%;
}

.vgt-table.nocturnal tr.clickable:hover {
  background-color: #445168
}

.vgt-table.nocturnal td {
  border-bottom: 1px solid #435169;
  color: #c7ced8
}

.vgt-table.nocturnal th.line-numbers, .vgt-table.nocturnal th.vgt-checkbox-col {
  color: #c7ced8;
  border-right: 1px solid #435169;
  background: linear-gradient(#2c394f, #2c394f)
}

.vgt-table.nocturnal thead th {
  color: #c7ced8;
  border-bottom: 1px solid #435169;
  background: linear-gradient(#2c394f, #2c394f)
}

.vgt-table.nocturnal thead th.sortable:before {
  border-top-color: #3e5170
}

.vgt-table.nocturnal thead th.sortable:after {
  border-bottom-color: #3e5170
}

.vgt-table.nocturnal thead th.sortable.sorting-asc {
  color: #fff
}

.vgt-table.nocturnal thead th.sortable.sorting-asc:after {
  border-bottom-color: #409eff
}

.vgt-table.nocturnal thead th.sortable.sorting-desc {
  color: #fff
}

.vgt-table.nocturnal thead th.sortable.sorting-desc:before {
  border-top-color: #409eff
}

.vgt-table.nocturnal.bordered td, .vgt-table.nocturnal.bordered th {
  border: 1px solid #435169
}

.vgt-table.nocturnal .vgt-input, .vgt-table.nocturnal .vgt-select {
  color: #c7ced8;
  background-color: #232d3f;
  border: 1px solid #435169
}

.vgt-table.nocturnal .vgt-input::placeholder, .vgt-table.nocturnal .vgt-select::placeholder {
  color: #c7ced8;
  opacity: .3
}

.vgt-table.nocturnal .v-select {
  background-color: #232d3f
}

.vgt-table.nocturnal .v-select input {
  color: #c7ced8
}

.vgt-table.nocturnal .v-select .vs__open-indicator {
  fill: #c7ced8
}

.vgt-wrap.nocturnal .vgt-wrap__footer {
  color: #c7ced8;
  border: 1px solid #435169;
  background: linear-gradient(#2c394f, #2c394f)
}

.vgt-wrap.nocturnal .vgt-wrap__footer .footer__row-count__label {
  color: #8290a7
}

.vgt-wrap.nocturnal .vgt-wrap__footer .footer__row-count__select {
  color: #c7ced8
}

.vgt-wrap.nocturnal .vgt-wrap__footer .footer__row-count__select:focus {
  border-color: #409eff
}

.vgt-wrap.nocturnal .vgt-wrap__footer .footer__navigation__page-btn {
  color: #c7ced8
}

.vgt-wrap.nocturnal .vgt-wrap__footer .footer__navigation__page-btn.disabled .chevron.left:after, .vgt-wrap.nocturnal .vgt-wrap__footer .footer__navigation__page-btn.disabled:hover .chevron.left:after {
  border-right-color: #c7ced8
}

.vgt-wrap.nocturnal .vgt-wrap__footer .footer__navigation__page-btn.disabled .chevron.right:after, .vgt-wrap.nocturnal .vgt-wrap__footer .footer__navigation__page-btn.disabled:hover .chevron.right:after {
  border-left-color: #c7ced8
}

.vgt-wrap.nocturnal .vgt-wrap__footer .footer__navigation__info, .vgt-wrap.nocturnal .vgt-wrap__footer .footer__navigation__page-info {
  color: #8290a7
}

.vgt-wrap.nocturnal .vgt-global-search {
  border: 1px solid #435169;
  background: linear-gradient(#2c394f, #2c394f)
}

.vgt-wrap.nocturnal .vgt-global-search__input .input__icon .magnifying-glass {
  border: 2px solid #3f4c63
}

.vgt-wrap.nocturnal .vgt-global-search__input .input__icon .magnifying-glass:before {
  background: #3f4c63
}

.vgt-wrap.nocturnal .vgt-global-search__input .vgt-input, .vgt-wrap.nocturnal .vgt-global-search__input .vgt-select {
  color: #c7ced8;
  background-color: #232d3f;
  border: 1px solid #435169
}

.vgt-wrap.nocturnal .vgt-global-search__input .vgt-input::placeholder, .vgt-wrap.nocturnal .vgt-global-search__input .vgt-select::placeholder {
  color: #c7ced8;
  opacity: .3
}

</style>

<script>


import { fromUnixTime, format } from 'date-fns'


import VueGoodTablePlugin from 'vue-good-table';
import 'vue-good-table/dist/vue-good-table.css'
import {VueGoodTable} from 'vue-good-table';
import jsonpipe from 'jsonpipe';

let msgServer;


// import axios from "axios";
//
// let apiUrl = window.location.protocol + "//" + window.location.hostname + ":8088";
// // let apiUrl = 'http://localhost:9080';
// // let apiUrl = 'http://request-handler:9080';
//
// let api = axios.create({
//   baseURL: apiUrl,
//   headers: {
//     'Content-Type': 'application/json'
//   }
// });

export default {
  name: 'result',
  props: ['results'],
  components: {
    VueGoodTable
  },
  filters: {
    percentage: function(value) {

      if (!value) {
        return "0%";
      }

      var result = Math.round((value + Number.EPSILON) * 10000) / 100;

      return result + "%";
    }
  },
  methods: {
    addQuiz(data) {

      // do not add in old records.
      if (data.timestamp < this.lastFetched) {
        return;
      }
      this.lastFetched = data.timestamp;

      if (data.answered > 0) {
        data.percentage = data.correct / data.answered;
        //data.percentage = Math.round(((data.correct/data.answered) + Number.EPSILON) * 100) / 100;
      } else {
        data.percentage = null;
      }
      const record = this.quizResults.find(f => f.quiz_id === data.quiz_id);
      window.console.log(record);
      if (record !== undefined) {
        window.console.log("updating " + data.quiz_id);
        Object.assign(record, data);
      } else {
        window.console.log("inserting " + data.quiz_id);
        this.quizResults.push(data);
      }
    }
  },
  data() {
    return {
      lastFetched: Date.now(),
      max: 10,
      quizIds: new Set(),
      quizResults: [],
      columns: [
        // {
        //   label: 'QuizId',
        //   field: 'quiz_id',
        //   filterable: true
        // },
        {
          label: 'Name',
          field: 'user_name',
          filterable: true
        },
        {
          label: 'Correct',
          field: 'correct',
          filterable: true,
          type: 'number'
        },
        {
          label: 'Answered',
          field: 'answered',
          filterable: true,
          type: 'number'
        },
        {
          label: 'Total',
          field: 'questions',
          filterable: true,
          type: 'number',
          hidden: true
        },
        {
          label: 'Score',
          field: 'percentage',
          filterable: true,
          type: 'percentage'
        },
        {
          label: 'Time',
          field: 'timestamp',
          filterable: true,
          type: 'number',
          formatFn: function(value) {
            const dt = fromUnixTime(Math.trunc(value/1000));
            return format(dt, "hh:mm:ss a");
          }
        }
      ]
    };
  },
  activated() {
    window.console.log("activated");
  },
  deactivated() {
    window.console.log("deactivated");
  },
  mounted() {
    window.console.log("mounted");

    let apiUrl = window.location.protocol + "//" + window.location.hostname + ":9081/leaderboard";

    window.console.log("mounted> " + apiUrl);

    this.$sse(apiUrl, {format: 'json'})
      .then(sse => {
        window.console.log(sse);

        // Store SSE object at a higher scope
        msgServer = sse;

        // Catch any errors (ie. lost connections, etc.)
        sse.onError(e => {
          console.log('lost connection; giving up!', e);
          // This is purely for example; EventSource will automatically
          // attempt to reconnect indefinitely, with no action needed
          // on your part to resubscribe to events once (if) reconnected
          sse.close();
        });

        // Listen for messages without a specified event
        sse.subscribe('', data => {

          window.console.log(data);
          this.addQuiz(data);

// //          if (!(this.quizIds.has(data.quiz_id))) {
//
//             data.percentage = Math.round(((data.correct/data.questions) + Number.EPSILON) * 10000) / 100;
//
//             this.quizIds.add(data.quiz_id);
//
//             this.quizResults.push(data);
//
//             // var topValues = this.quizResults.sort((a,b) => {
//             //   var x = (b.correct/b.questions) - (a.correct/a.questions);
//             //
//             //   if (x === 0) {
//             //     x = (b.timestamp - a.timestamp);
//             //   }
//             //
//             //   return x;
//             // });//.slice(0,this.max);
//             //
//             // this.quizResults = topValues;
//
// //          }
//           window.console.log(data);
//           // this.messages.push(data);
//           // var container = this.$el.querySelector("#eventTable");
//           // container.scrollTop = container.scrollHeight;
        });

      })
      .catch(err => {
        // When this error is caught, it means the initial connection to the
        // events server failed.  No automatic attempts to reconnect will be made.
        console.log('Failed to connect to server', err);
      });
    // api.post("/query", {
    //   "ksql": "select r.quiz_id, u.name name, r.questions total, r.correct correct from results r join users u on r.user_id = u.user_id emit changes;",
    //   "streamsProperties": {"ksql.streams.auto.offset.reset": "earliest"}
    // }).then(response => {
    //   window.console.log(response);
    // })
  },
  beforeDestroy() {
    window.console.log("beforeDestroy");
    // Make sure to close the connection with the events server
    // when the component is destroyed, or we'll have ghost connections!
    if (msgServer !== undefined) {
      window.console.log("closing SSE connection.");
      msgServer.close();
    }
  }
};
</script>