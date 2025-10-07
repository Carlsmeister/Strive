package se.umu.calu0217.strive.data.mappers

import se.umu.calu0217.strive.data.local.entities.ExerciseEntity
import se.umu.calu0217.strive.data.remote.ExerciseDto
import se.umu.calu0217.strive.domain.models.Exercise

// Entity -> Domain
fun ExerciseEntity.toDomainModel(): Exercise {
    return Exercise(
        id = id,
        name = name,
        bodyParts = bodyParts,
        equipment = equipment,
        instructions = instructions,
        imageUrl = imageUrl
    )
}

// Remote DTO -> Entity
fun ExerciseDto.toEntity(): ExerciseEntity {
    val imageUrl = when {
        !this.gifUrl.isNullOrBlank() -> this.gifUrl
        this.images.isNotEmpty() -> this.images.first()
        else -> null
    }
    return ExerciseEntity(
        id = this.id.hashCode().toLong(),
        name = this.name,
        bodyParts = listOf(this.bodyPart, this.target) + this.secondaryMuscles.takeIf { it.isNotEmpty() }.orEmpty(),
        equipment = this.equipment,
        instructions = if (this.instructions.isNotEmpty()) this.instructions else listOf("No instructions provided."),
        imageUrl = imageUrl
    )
}
