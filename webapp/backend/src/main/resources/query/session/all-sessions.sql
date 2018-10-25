--! #Lh
WITH sessdata as
(select
  P.SPID
  ,CASE SUBSTR(S.state,1,7)
    WHEN 'WAITING' THEN 'WAIT'
    WHEN 'WAITED ' THEN 'CPU'
    ELSE 'N/A'
  END EVENT_TYPE
  ,S.username USERNAME
  ,S.event    EVENT
  ,S.MODULE
  ,S.SID
  ,S.AUDSID
  ,S.serial# SERIAL
  ,S.SQL_ID
  ,S.STATUS
  ,t.start_time
  ,S.MACHINE
  ,S.SECONDS_IN_WAIT
  ,a.SQL_TEXT
  ,case when substr(S.state,1,7) = 'CPU' then 'CPU' else n.wait_class end wait_class
  ,(
  	select
  	  rtrim(xmlagg(xmlelement(e,plan_table_output,'~').EXTRACT('//text()') ORDER BY rownum).GetClobVal(),'~' )
  	  from table(dbms_xplan.display_cursor(sql_id => S.SQL_ID, format => 'basic'))
  ) EXECUTION_PLAN
  from
     v$session      S
    ,v$process      P
    ,v$transaction  t
    ,v$sqlarea      a
    ,v$event_name   n
  where
        s.event       = n.name
    and S.type       != 'BACKGROUND'
    and S.wait_class != 'Idle'
    and S.paddr       = P.addr
    and S.taddr       = t.addr(+)
    and s.sql_hash_value = a.hash_value (+)
    and s.sql_address    = a.address (+)
 )
select * from sessdata
where SQL_TEXT not like '--! #Lh%'
order by start_time