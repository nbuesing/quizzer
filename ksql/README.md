
create stream QUIZ_NEXT  with (KAFKA_TOPIC='quiz_next', VALUE_FORMAT='avro');

create stream QUIZ_RESULT with (KAFKA_TOPIC='quiz_result', VALUE_FORMAT='avro');

create table KSQL_QUIZ_NEXT as 
select request_id, 
       latest_by_offset(quiz_id) as quiz_id, 
       latest_by_offset(user_id) as user_id, 
       latest_by_offset(question_id) as question_id,
       latest_by_offset(statement) as statement,
       latest_by_offset(a) as a,
       latest_by_offset(b) as b,
       latest_by_offset(c) as c, 
       latest_by_offset(d) as d, 
       latest_by_offset(difficulty) as difficulty 
  from quiz_next 
window tumbling (size 30 seconds)
 group by request_id;

create table KSQL_QUIZ_RESULT as 
select request_id, 
       latest_by_offset(quiz_id) as quiz_id, 
       latest_by_offset(user_id) as user_id, 
       latest_by_offset(user_name) as user_name, 
       latest_by_offset(questions) as questions, 
       latest_by_offset(correct) as correct 
  from quiz_result
window tumbling (size 30 seconds)
 group by request_id;
























##
##
##








create table QUIZ_NEXT with (KAFKA_TOPIC='quiz_next', VALUE_FORMAT='avro');

create table QUIZ_RESULT with (KAFKA_TOPIC='quiz_result', VALUE_FORMAT='avro');

create table QUIZ_NEXT (ROWKEY VARCHAR PRIMARY KEY) with (KAFKA_TOPIC='quiz_next', VALUE_FORMAT='avro');

create table QUIZ_RESULT (ROWKEY VARCHAR PRIMARY KEY) with (KAFKA_TOPIC='quiz_result', VALUE_FORMAT='avro');










create table KSQL_QUIZ_NEXT as SELECT last_by_offset(QUIZ_ID) as QUIZ_ID, USER_ID, QUESTION_ID, STATEMENT, A, B, C, D, DIFFICULTY from QUIZ_NEXT group by USER_ID, QUESTION_ID, STATEMENT, A, B, C, D, DIFFICULTY;

create table KSQL_QUIZ_NEXT as SELECT latest_by_offset(QUIZ_ID) as QUIZ_ID, USER_ID, QUESTION_ID, STATEMENT, A, B, C, D, DIFFICULTY from QUIZ_NEXT group by USER_ID, QUESTION_ID, STATEMENT, A, B, C, D, DIFFICULTY


create stream QUIZ_RESULT with (KAFKA_TOPIC='quiz_result', VALUE_FORMAT='avro');

create stream QUIZ_NEXT  with (KAFKA_TOPIC='quiz_next', VALUE_FORMAT='avro');
create table KSQL_QUIZ_NEXT as select quiz_id, latest_by_offset(concat(user_id + '|', question_id + '|' + statement + '|' + a + '|' + b + '|' + c + '|' + d + '|' + cast(difficulty as varchar))) as response from quiz_next group by quiz_id;



#
#
#


create table KSQL_QUIZ_NEXT3 as 
select quiz_id, 
       latest_by_offset(user_id) as user_id, 
       latest_by_offset(question_id) as question_id,
       latest_by_offset(statement) as statement,
       latest_by_offset(a) as a,
       latest_by_offset(b) as b,
       latest_by_offset(c) as c, 
       latest_by_offset(d) as d, 
       latest_by_offset(difficulty) as difficulty, 
       latest_by_offset(request_id) as request_id 
  from quiz_next 
 group by quiz_id;

create table KSQL_QUIZ_RESULT3 as 
select quiz_id, 
       latest_by_offset(user_id) as user_id, 
       latest_by_offset(user_name) as user_name, 
       latest_by_offset(questions) as questions, 
       latest_by_offset(correct) as correct,
       latest_by_offset(request_id) as request_id 
  from quiz_result
 group by quiz_id;























  as_map(
    array['user_id', 'user_name', 'questions', 'correct'],
    split(latest_by_offset(concat(user_id + '|', user_name + '|' + cast(questions as varchar) + '|' + cast(correct as varchar))), '|')
  ) as response 
  from quiz_result
  group by quiz_id;


create table KSQL_QUIZ_NEXT2 
    as 
select 
  quiz_id, 
  as_map(
    array['user_id', 'question_id', 'statement', 'a', 'b', 'c', 'd', 'difficulty'],
    split(latest_by_offset(concat(user_id + '|', question_id + '|' + statement + '|' + a + '|' + b + '|' + c + '|' + d + '|' + cast(difficulty as varchar))), '|')
  ) as response 
  from quiz_next 
  group by quiz_id;

create table KSQL_QUIZ_RESULT2 
    as 
select 
  quiz_id, 
  as_map(
    array['user_id', 'user_name', 'questions', 'correct'],
    split(latest_by_offset(concat(user_id + '|', user_name + '|' + cast(questions as varchar) + '|' + cast(correct as varchar))), '|')
  ) as response 
  from quiz_result
  group by quiz_id;


QUIZ_ID                                   |USER_ID                                   |USER_NAME                                 |QUESTIONS                                 |CORRECT                                   |REQUEST_ID


