--Write MySQL query to find IPs that mode more than a certain number of requests for a given time period.

select ip, count(*) from logs
 where date >= '2017-01-01.13:00:00' and date < '2017-01-01.14:00:00'
 group by ip
 having count(*) > 100
