count=1
#while [ $count -lt 100 ];
while [ 1 ];
do
  echo $count
  json=`echo "{'create':{'date':1452739584192,'orderId':0,'account':{'accountId':$count,'accountName':'CX2001'},'security':{'securityId':1,'symbol':'AAPL','name':'Apple','securityType':'STOCK'},'orderLegs':[{'side':'BUY','price':169.8044039237485,'quantity':1.8266133613162219},{'side':'BUY','price':124.19051098410604,'quantity':3.712437447443148},{'side':'BUY','price':189.60632297826584,'quantity':2.309947392963119},{'side':'BUY','price':114.52785235488147,'quantity':1.8134820240706453},{'side':'BUY','price':122.59536943620424,'quantity':5.656201182112825},{'side':'BUY','price':119.93583371454665,'quantity':7.787093283285209},{'side':'BUY','price':133.77282254294673,'quantity':7.431309494934907},{'side':'BUY','price':141.05542937485723,'quantity':10.01755275286581},{'side':'BUY','price':180.52038480218042,'quantity':5.310141656292994},{'side':'BUY','price':130.52040097863588,'quantity':3.21403285548609}]}}"|sed "s/'/\"/g"`
  curl -X POST http://localhost:8181/orders -H 'Content-Type: application/json' -d $json
  curl -H 'Content-Type: application/json' http://localhost:8181/orders/account?accountId=$count 
  count=`expr $count + 1`
  e=`expr $count % 100`
  if [ $e -eq 0 ];
  then 
      sleep 1
  fi;
done
