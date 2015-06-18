SELECT instance_pseudo_id,questionId,answer FROM 
	${repeat_set_instance} as a 
	INNER JOIN ${rpt_set_ins_to_ques_id} as b 
	ON a.id=b.rpt_ins_id 
	INNER JOIN ${data_table} as c
	ON c.id = b.ques_id
	WHERE a.survey=?
	AND a.inviteeId=?
	AND a.repeat_set_name=?
	ORDER BY c.id 
