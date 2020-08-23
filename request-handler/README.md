
SET 'auto.offset.reset'='earliest';

create stream results with (KAFKA_TOPIC='quiz_result', VALUE_FORMAT='avro', KEY='QUIZ_ID');
create table users with (KAFKA_TOPIC='users', VALUE_FORMAT='avro', KEY='USER_ID');
select r.quiz_id, u.name name, r.questions total, r.correct correct from results r join users u on r.user_id = u.user_id emit changes;


curl -X "POST" "http://localhost:8088/query" \
     -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
     -d $'{
  "ksql": "select r.quiz_id, u.name name, r.questions total, r.correct correct from results r join users u on r.user_id = u.user_id emit changes;",
  "streamsProperties": {"ksql.streams.auto.offset.reset": "earliest"}
}'







# designed to where you only get question 1 at a time, since answering correct or incorrect could result in different next questions.
# quizId is globally unique -- no security is in place

questions:
  KTable
  key: QUESTION_ID
  value: { QUESTION_ID, STATEMENT, A, B, C, D, CORRECT_ANSWER }
    
users:
  KTable
  key: USER_ID
  value: { USER_ID, NAME }

quizzes:
  KTable
  key: QUIZ_ID
  value { quizId, userId, count, currentCount }
  
current-question:
  KTable
  key: QUIZ_ID
  value: { QUESTION_ID }

quiz-questions:
  KTable:
  key: quizId:#
  value: { questionId, submittedAnswer, correctAnswer }

  
next-question:
  KStream
  key: QUIZ_ID
  value: { QUESTION_ID, STATEMENT, A, B, C, D }
 
start-quiz:
  KStream:
  key: userId
  value: { QUIZ_ID, USER_ID, COUNT }

submit-answer:
  KStream
  key: quizId
  value: { questionId, submittedAnswer }  
  

 
  

quizzes:
  KStream (KTable)
  key: userId
  value: quizId
  

next-question:
  KStream
  key: quizId
  value: { questionId, statement, a, b, c, d }


inprogress:
  KTable
  key: quizId
  value: quiz { quizId, userId, ...}

results:
  KTable
  key: userId, quizId
  value: quiz { quizId, userId, numberOfQuestions, correct, incorrect }
  

current-question:
  KTable
  key: quizId
  value: questionId


create table questions with (KAFKA_TOPIC='questions', VALUE_FORMAT='avro', KEY='QUESTION_ID');
create table users with (KAFKA_TOPIC='users', VALUE_FORMAT='avro', KEY='USER_ID');

create table quiz_start with (KAFKA_TOPIC='quiz_start', VALUE_FORMAT='avro', KEY='QUIZ_ID');
create stream quiz_next with (KAFKA_TOPIC='quiz_next', VALUE_FORMAT='avro', KEY='QUIZ_ID');
create stream quiz_answer with (KAFKA_TOPIC='quiz_answer', VALUE_FORMAT='avro', KEY='QUIZ_ID');

create stream quiz_question as
  select a.QUIZ_ID QUIZ_ID, a.QUESTION_ID QUESTION_ID, a.SUBMITTED_ANSWER, q.CORRECT_ANSWER, (a.SUBMITTED_ANSWER = q.CORRECT_ANSWER) RESULT
  from quiz_answer a
  join questions q on q.QUESTION_ID = a.QUESTION_ID
  partition by QUIZ_ID;

create stream quiz_next_1b as
  select QUIZ_ID, (QUESTION_ID + 1) as QUESTION_ID
  from quiz_question
  partition by QUIZ_ID;
  

create stream quiz_next as
  select n.QUIZ_ID QUIZ_ID, q.QUESTION_ID QUESTION_ID, q.STATEMENT, q.A, q.B, q.B, q.C, q.D 
  from quiz_next_1 n
  join question q on q.QUESTION_ID = n.QUESTION_ID;
   











create stream quiz_question as
  select a.quizId QUIZ_ID, a.QUESTION_ID, a.SUBMITTED_ANSWER, q.CORRECT_ANSWER, (a.SUBMITTED_ANSWER = q.CORRECT_ANSWER) result
  from quiz_question a
  join questions q on q.QUESTION_ID = a.QUESTION_ID
  partition by q.quizId;


create stream next_question_1 as
  select g.QUIZ_ID QUIZ_ID, g.questionId questionId, g.submitted submitted, g.correct correct, g.result result, (q.nextQuestionId + 1) nextQuestionId
  from quiz_answer g
  join quiz_start q on g.QUIZ_ID = q.QUIZ_ID;



create table quizzes as
  select QUIZ_ID, USER_ID, COUNT, 0 as CURRENT_COUNT, MAX(COUNT)
  from quiz_start
  group by quiz_id, user_id, count;
  
  
create stream next_question_1 as
  select g.QUIZ_ID QUIZ_ID, g.questionId questionId, g.submitted submitted, g.correct correct, g.result result, (q.nextQuestionId + 1) nextQuestionId
  from quiz_answer g
  join quiz_start q on g.QUIZ_ID = q.QUIZ_ID;
  
  
  
  
  
 

create stream quizzes with (KAFKA_TOPIC='quizzes', VALUE_FORMAT='avro', KEY='QUIZ_ID');

create stream graded_answers as
  select a.quizId quizId, a.questionId questionId, a.answer submitted, q.answer correct, (a.answer = q.answer) result
  from answers a
  join questions q on q.questionId = a.questionId
  partition by q.quizId;

create stream next_question_1 as
  select g.quizId quizId, g.questionId questionId, g.submitted submitted, g.correct correct, g.result result, (q.nextQuestionId + 1) nextQuestionId
  from graded_answers g
  join quizzes q on g.quizId = q.quizId;

create stream next_question as
  select q.questionId questionId, q.statement, q.a, q.b, q.c, q.d
  from t1a
  join questions q on t1a.nextQuestionId = q.questionId;

create stream current_question as
  select questionId, quizId
  from next_question_1
  partition by quizId;
  
insert into current_question
  select 1, quizId
  from quizzes;


  
  

create stream t1 as
  select g.quizId quizId, g.questionId questionId, g.submitted submitted, g.correct correct, g.result result, 2 nextQuestionId
  from graded_answers g;

create stream t2 as
  select q.questionId questionId, q.statement, q.a, q.b, q.c, q.d
  from t1
  join questions q on t1.nextQuestionId = q.questionId;
   

create stream graded_answers_next_question as
  select g.quizId quizId, g.questionId questionId, g.submitted submitted, g.correct correct, g.result result, q.nextQuestionId nextQuestionId
  from graded_answers g
  join quizzes q on q.quizId = g.quizId
  partition by g.quizId;
  
create stream next_question as
  select * from graded_answers

create stream next_question with (KAFKA_TOPIC='next_question') as
  select qt.* from answers a
  join quizzes qz on q.quizId = a.quizId
  join question qt on qt = 2
  partition by a.quizId;

create stream next as
  select qa.questionId + 1 from graded_answers ga;


select 
    (a.answer = q.answer) as is_correct, 
    * 
    from answers a 
    join questions q on a.questionid = q.questionid
    join users u on a.userid = u.userid;
    