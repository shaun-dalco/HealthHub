<?php

declare(strict_types=1);

namespace OCA\HealthHub\Migration;

use Closure;
use OCP\DB\ISchemaWrapper;
use OCP\DB\Types;
use OCP\Migration\IOutput;
use OCP\Migration\SimpleMigrationStep;

class Version010100Date20251126000000 extends SimpleMigrationStep {

	public function preSchemaChange(IOutput $output, Closure $schemaClosure, array $options) {
	}

	public function changeSchema(IOutput $output, Closure $schemaClosure, array $options) {
		/** @var ISchemaWrapper $schema */
		$schema = $schemaClosure();

		// 1) Overview table (one row per user per day)
		if (!$schema->hasTable('healthhub_overview')) {
			$table = $schema->createTable('healthhub_overview');
			$table->addColumn('id', Types::BIGINT, [
				'autoincrement' => true,
				'notnull' => true,
			]);
			$table->addColumn('user_id', Types::STRING, [
				'notnull' => true,
				'length' => 64,
			]);
			$table->addColumn('date', Types::STRING, [
				'notnull' => true,
				'length' => 10, // 'YYYY-MM-DD'
			]);
			$table->addColumn('total_steps', Types::INTEGER, [
				'notnull' => true,
			]);
			$table->addColumn('avg_hr', Types::INTEGER, [
				'notnull' => true,
			]);
			$table->addColumn('max_hr', Types::INTEGER, [
				'notnull' => true,
			]);
			$table->addColumn('min_hr', Types::INTEGER, [
				'notnull' => true,
			]);
			$table->addColumn('sleep_hours', Types::FLOAT, [
				'notnull' => true,
			]);
			$table->addColumn('weight_kg', Types::FLOAT, [
				'notnull' => true,
			]);
			$table->addColumn('updated_at', Types::INTEGER, [
				'notnull' => true,
			]);
			$table->setPrimaryKey(['id']);
			$table->addIndex(['user_id', 'date'], 'healthhub_overview_uid_date');
		}

		// 2) Heart rate timeseries
		if (!$schema->hasTable('healthhub_heart_rate')) {
			$table = $schema->createTable('healthhub_heart_rate');
			$table->addColumn('id', Types::BIGINT, [
				'autoincrement' => true,
				'notnull' => true,
			]);
			$table->addColumn('user_id', Types::STRING, [
				'notnull' => true,
				'length' => 64,
			]);
			$table->addColumn('timestamp', Types::INTEGER, [
				'notnull' => true, // unix timestamp
			]);
			$table->addColumn('bpm', Types::INTEGER, [
				'notnull' => true,
			]);
			$table->setPrimaryKey(['id']);
			$table->addIndex(['user_id', 'timestamp'], 'healthhub_hr_uid_ts');
		}

		// 3) Steps
		if (!$schema->hasTable('healthhub_steps')) {
			$table = $schema->createTable('healthhub_steps');
			$table->addColumn('id', Types::BIGINT, [
				'autoincrement' => true,
				'notnull' => true,
			]);
			$table->addColumn('user_id', Types::STRING, [
				'notnull' => true,
				'length' => 64,
			]);
			$table->addColumn('date', Types::STRING, [
				'notnull' => true,
				'length' => 10,
			]);
			$table->addColumn('count', Types::INTEGER, [
				'notnull' => true,
			]);
			$table->setPrimaryKey(['id']);
			$table->addIndex(['user_id', 'date'], 'healthhub_steps_uid_date');
		}

		// 4) Sleep
		if (!$schema->hasTable('healthhub_sleep')) {
			$table = $schema->createTable('healthhub_sleep');
			$table->addColumn('id', Types::BIGINT, [
				'autoincrement' => true,
				'notnull' => true,
			]);
			$table->addColumn('user_id', Types::STRING, [
				'notnull' => true,
				'length' => 64,
			]);
			$table->addColumn('date', Types::STRING, [
				'notnull' => true,
				'length' => 10,
			]);
			$table->addColumn('duration_hours', Types::FLOAT, [
				'notnull' => true,
			]);
			$table->addColumn('quality_score', Types::INTEGER, [
				'notnull' => true,
			]);
			$table->setPrimaryKey(['id']);
			$table->addIndex(['user_id', 'date'], 'healthhub_sleep_uid_date');
		}

		// 5) Weight
		if (!$schema->hasTable('healthhub_weight')) {
			$table = $schema->createTable('healthhub_weight');
			$table->addColumn('id', Types::BIGINT, [
				'autoincrement' => true,
				'notnull' => true,
			]);
			$table->addColumn('user_id', Types::STRING, [
				'notnull' => true,
				'length' => 64,
			]);
			$table->addColumn('date', Types::STRING, [
				'notnull' => true,
				'length' => 10,
			]);
			$table->addColumn('kg', Types::FLOAT, [
				'notnull' => true,
			]);
			$table->setPrimaryKey(['id']);
			$table->addIndex(['user_id', 'date'], 'healthhub_weight_uid_date');
		}

		return $schema;
	}

	public function postSchemaChange(IOutput $output, Closure $schemaClosure, array $options) {
	}
}
