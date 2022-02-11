# MySQL Connector/J sendFractionalSeconds=true|false Performance Test

[MySQL Connector/J 5.1.37](https://dev.mysql.com/doc/relnotes/connector-j/5.1/en/news-5-1-37.html) 부터 `sendFractionalSeconds=false` 라는 프라퍼티 설정을 통해서
`Date` 객체 전송시 fractional seconds 부분을 절삭해서 전송함으로써 MySQL 5.6 부터 발생하는 fractionalSeconds 반올림 현상을 막을 수 있게 되었다.

이 옵션이 `true`,`false` 일 때에 따라 성능상의 차이는 없는지 테스트 해보았다.

결론 : 성능 차이가 없었다.
