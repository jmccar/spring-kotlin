package com.kousenit.persistence.dao

import com.kousenit.persistence.entities.Officer
import com.kousenit.persistence.entities.Rank
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.queryForObject
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.*

@Repository
class JdbcOfficerDAO(private val jdbcTemplate: JdbcTemplate) : OfficerDAO {

    private val officerMapper = RowMapper { rs: ResultSet, _ ->
        Officer(id = rs.getInt("id"),
                rank = Rank.valueOf(rs.getString("rank")),
                first = rs.getString("first_name"),
                last = rs.getString("last_name"))
    }

    private val insertOfficer = SimpleJdbcInsert(jdbcTemplate)
            .withTableName("officers")
            .usingGeneratedKeyColumns("id")

    override fun count() =
            jdbcTemplate.queryForObject<Long>("select count(*) from officers")

    override fun delete(officer: Officer) {
        jdbcTemplate.update("delete from officers where id=?", officer.id)
    }

    override fun existsById(id: Int) =
            jdbcTemplate.queryForObject(
                    "select exists(select 1 from officers where id=?)",
                    Boolean::class.java, id)

    override fun findById(id: Int)=
            if (!existsById(id)) Optional.empty() else
                jdbcTemplate.queryForObject("SELECT * FROM officers WHERE id=?",
                        officerMapper, id)?.let { Optional.of(it) } ?: Optional.empty()

    override fun findAll(): List<Officer> =
            jdbcTemplate.query<Officer>("select * from officers", officerMapper)

    override fun save(officer: Officer) =
            insertOfficer.executeAndReturnKey(
                            mapOf("rank" to officer.rank,
                                    "first_name" to officer.first,
                                    "last_name" to officer.last))
                    .let {
                        officer.id = it.toInt()
                        officer
                    }

}