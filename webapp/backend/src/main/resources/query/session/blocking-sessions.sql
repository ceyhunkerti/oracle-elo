--! #Lh
select
  p1.spid blocking_spid,
  p2.spid blocked_spid,
  s1.audsid blocking_audsid,
  s2.audsid blocked_audsid,
  s1.sid blocking_sid,
  s2.sid blocked_sid,
  s1.username blocking_username,
  s2.username blocked_username,
  s1.machine blocking_machine,
  s2.machine blocked_machine,
  s1.module blocking_module,
  s2.module blocked_module,
  s1.serial# blocking_serial,
  s2.serial# blocked_serial,
  s1.sql_id blocking_sql_id,
  s2.sql_id blocked_sql_id,
  s1.event blocking_event,
  s2.event blocked_event,
  s1.status blocking_status,
  s2.status blocked_status,
  s1.machine blocking_machine,
  s2.machine blocked_machine
from
  v$lock l1, v$session s1, v$lock l2, v$session s2, v$process p1, v$process p2
where
  s1.sid=l1.sid
  and s2.sid=l2.sid
  and l1.BLOCK=1
  and l2.request > 0
  and l1.id1 = l2.id1
  and l2.id2 = l2.id2
  and s1.paddr = p1.addr(+)
  and s2.paddr = p2.addr(+)